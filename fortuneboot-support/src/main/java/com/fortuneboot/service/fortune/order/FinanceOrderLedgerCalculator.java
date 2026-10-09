package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderSettlementStateEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 单据账本纯计算器
 * <p>
 * 不访问数据库、不读系统当前时间；dueDate/overdue由QueryService使用Clock计算。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public class FinanceOrderLedgerCalculator {

    /**
     * 单笔新金额上限
     */
    private static final BigDecimal MAX_ENTRY_AMOUNT = new BigDecimal("999999999999.99");

    /**
     * DECIMAL(20,4)聚合容量上限
     */
    private static final BigDecimal MAX_AGGREGATE_AMOUNT = new BigDecimal("9999999999999999.9999");

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * 形成本金的流水类型：垫付/借出/借入
     */
    private static final Set<Integer> PRINCIPAL_OUT_BILL_TYPES = Set.of(7, 9, 12);

    /**
     * 结算本金的流水类型：报销到账/收回本金/归还本金
     */
    private static final Set<Integer> PRINCIPAL_IN_BILL_TYPES = Set.of(8, 10, 11);

    /**
     * 单据类型 + 组件 + 流水类型 完整白名单
     */
    private static final Map<Integer, Map<FinanceOrderComponentEnum, Set<Integer>>> WHITELIST = Map.of(
            FinanceOrderTypeEnum.EXPENSE_CLAIM.getValue(), Map.of(
                    FinanceOrderComponentEnum.PRINCIPAL, Set.of(7, 8),
                    FinanceOrderComponentEnum.FEE, Set.of(1),
                    FinanceOrderComponentEnum.WRITE_OFF, Set.of(1)),
            FinanceOrderTypeEnum.LOAN_OUT.getValue(), Map.of(
                    FinanceOrderComponentEnum.PRINCIPAL, Set.of(9, 10),
                    FinanceOrderComponentEnum.INTEREST, Set.of(2),
                    FinanceOrderComponentEnum.FEE, Set.of(1),
                    FinanceOrderComponentEnum.WRITE_OFF, Set.of(1)),
            FinanceOrderTypeEnum.LOAN_IN.getValue(), Map.of(
                    FinanceOrderComponentEnum.PRINCIPAL, Set.of(11, 12),
                    FinanceOrderComponentEnum.INTEREST, Set.of(1),
                    FinanceOrderComponentEnum.FEE, Set.of(1),
                    FinanceOrderComponentEnum.WRITE_OFF, Set.of(2))
    );

    /**
     * 从有效流水重建单据账本汇总
     *
     * @param orderType 单据类型
     * @param entries   关联流水（包含待确认、已作废、已删除与历史版本记录）
     * @return 单据汇总快照
     */
    public FinanceOrderLedgerSnapshot calculate(Integer orderType, List<FinanceOrderLedgerEntry> entries) {
        FinanceOrderTypeEnum orderTypeEnum = validateOrderType(orderType);
        List<FinanceOrderLedgerEntry> safeEntries = Objects.requireNonNullElse(entries, List.of());

        Ledger ledger = new Ledger();
        for (FinanceOrderLedgerEntry entry : safeEntries) {
            collect(orderTypeEnum, entry, ledger);
        }

        validateAggregateCapacity("本金出向", ledger.outAmount);
        validateAggregateCapacity("本金入向", ledger.inAmount);
        validateAggregateCapacity("差额结清", ledger.adjustedAmount);
        validateAggregateCapacity("利息收入", ledger.interestIncome);
        validateAggregateCapacity("利息支出", ledger.interestExpense);
        validateAggregateCapacity("手续费", ledger.feeAmount);

        boolean loanIn = Objects.equals(orderTypeEnum, FinanceOrderTypeEnum.LOAN_IN);
        BigDecimal issuedAmount = loanIn ? ledger.inAmount : ledger.outAmount;
        BigDecimal settledAmount = loanIn ? ledger.outAmount : ledger.inAmount;
        BigDecimal pendingAmount = issuedAmount.subtract(settledAmount).subtract(ledger.adjustedAmount);
        BigDecimal settledPlusAdjusted = settledAmount.add(ledger.adjustedAmount);

        if (pendingAmount.compareTo(ZERO) < 0) {
            throw new ApiException(ErrorCode.Business.ORDER_OVER_SETTLED,
                settledPlusAdjusted.toPlainString(), issuedAmount.toPlainString());
        }

        BigDecimal interestPlusFee = ledger.interestIncome.add(ledger.interestExpense).add(ledger.feeAmount);
        if (ledger.hasEffectiveInterestOrFee && issuedAmount.compareTo(ZERO) == 0) {
            throw new ApiException(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID,
                interestPlusFee.toPlainString(), pendingAmount.toPlainString());
        }

        validateTimePrefix(ledger.scanEntries);

        BigDecimal netCostOrIncome = ledger.interestIncome
                .subtract(ledger.interestExpense)
                .subtract(ledger.feeAmount)
                .add(loanIn ? ledger.adjustedAmount : ledger.adjustedAmount.negate());
        validateAggregateCapacity("净收支", netCostOrIncome);

        String settlementState = resolveSettlementState(issuedAmount, settledPlusAdjusted,
            pendingAmount, ledger.hasEffectiveEntry);

        return new FinanceOrderLedgerSnapshot(
                ledger.outAmount, ledger.inAmount, issuedAmount, settledAmount,
                ledger.adjustedAmount, pendingAmount, ledger.interestIncome,
                ledger.interestExpense, ledger.feeAmount, netCostOrIncome,
                ledger.pendingEntryCount, settlementState);
    }

    private void collect(FinanceOrderTypeEnum orderType, FinanceOrderLedgerEntry entry, Ledger ledger) {
        if (Objects.isNull(entry)) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "空流水记录");
        }
        if (entry.ledgerVersion() != 1) {
            if (entry.ledgerVersion() != 0) {
                throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "非法流水账本版本: " + entry.ledgerVersion());
            }
            return;
        }
        if (entry.deleted() || entry.voided()) {
            return;
        }

        FinanceOrderComponentEnum component = validateComponent(entry.component());
        validateWhitelist(orderType, component, entry.billType());
        BigDecimal amount = validateAmount(entry.amount());
        validateTradeTime(entry);

        if (!entry.confirmed()) {
            ledger.pendingEntryCount++;
            return;
        }

        ledger.hasEffectiveEntry = true;
        switch (component) {
            case PRINCIPAL -> {
                boolean issuedSide = Objects.equals(orderType, FinanceOrderTypeEnum.LOAN_IN)
                        == PRINCIPAL_IN_BILL_TYPES.contains(entry.billType());
                if (PRINCIPAL_OUT_BILL_TYPES.contains(entry.billType())) {
                    ledger.outAmount = ledger.outAmount.add(amount);
                    ledger.scanEntries.add(new ScanEntry(entry.billId(), amount,
                            issuedSide ? amount : amount.negate(),
                            entry.tradeTime(), ledger.nextScanIndex()));
                } else {
                    ledger.inAmount = ledger.inAmount.add(amount);
                    ledger.scanEntries.add(new ScanEntry(entry.billId(), amount,
                            issuedSide ? amount : amount.negate(),
                            entry.tradeTime(), ledger.nextScanIndex()));
                }
            }
            case INTEREST -> {
                ledger.hasEffectiveInterestOrFee = true;
                if (Objects.equals(entry.billType(), 2)) {
                    ledger.interestIncome = ledger.interestIncome.add(amount);
                } else {
                    ledger.interestExpense = ledger.interestExpense.add(amount);
                }
            }
            case FEE -> {
                ledger.hasEffectiveInterestOrFee = true;
                ledger.feeAmount = ledger.feeAmount.add(amount);
            }
            case WRITE_OFF -> {
                ledger.adjustedAmount = ledger.adjustedAmount.add(amount);
                ledger.scanEntries.add(new ScanEntry(entry.billId(), amount, amount.negate(),
                        entry.tradeTime(), ledger.nextScanIndex()));
            }
        }
    }

    private FinanceOrderTypeEnum validateOrderType(Integer orderType) {
        for (FinanceOrderTypeEnum orderTypeEnum : FinanceOrderTypeEnum.values()) {
            if (Objects.equals(orderTypeEnum.getValue(), orderType)) {
                return orderTypeEnum;
            }
        }
        throw new ApiException(ErrorCode.Business.ORDER_TYPE_MISMATCH, String.valueOf(orderType));
    }

    private FinanceOrderComponentEnum validateComponent(String component) {
        FinanceOrderComponentEnum componentEnum = FinanceOrderComponentEnum.getByValue(component);
        if (Objects.isNull(componentEnum)) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "未知单据组件: " + component);
        }
        return componentEnum;
    }

    private void validateWhitelist(FinanceOrderTypeEnum orderType, FinanceOrderComponentEnum component, Integer billType) {
        Set<Integer> allowedBillTypes = WHITELIST.get(orderType.getValue()).get(component);
        if (Objects.isNull(allowedBillTypes) || !allowedBillTypes.contains(billType)) {
            throw new ApiException(ErrorCode.Business.ORDER_TYPE_MISMATCH,
                "type=" + orderType.getValue() + ",billType=" + billType + ",component=" + component.getValue());
        }
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (Objects.isNull(amount) || amount.compareTo(ZERO) <= 0) {
            throw amountInvalid(amount);
        }
        BigDecimal normalized;
        try {
            normalized = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw amountInvalid(amount);
        }
        if (normalized.compareTo(MAX_ENTRY_AMOUNT) > 0) {
            throw amountInvalid(amount);
        }
        return normalized;
    }

    private ApiException amountInvalid(BigDecimal amount) {
        return new ApiException(ErrorCode.Business.ORDER_AMOUNT_INVALID, amountText(amount));
    }

    private void validateTradeTime(FinanceOrderLedgerEntry entry) {
        if (Objects.isNull(entry.tradeTime())) {
            throw new ApiException(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID,
                amountText(entry.amount()), "未知");
        }
    }

    private void validateAggregateCapacity(String name, BigDecimal value) {
        if (value.abs().compareTo(MAX_AGGREGATE_AMOUNT) > 0) {
            throw new ApiException(ErrorCode.Business.ORDER_AMOUNT_INVALID,
                name + "累计超过数据库容量: " + value.abs().toPlainString());
        }
    }

    private void validateTimePrefix(List<ScanEntry> scanEntries) {
        List<ScanEntry> sorted = new ArrayList<>(scanEntries);
        sorted.sort(Comparator
                .comparing(ScanEntry::tradeTime)
                .thenComparing(ScanEntry::billId, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(ScanEntry::originalIndex));

        BigDecimal runningPending = ZERO;
        for (ScanEntry scanEntry : sorted) {
            runningPending = runningPending.add(scanEntry.delta());
            if (runningPending.compareTo(ZERO) < 0) {
                throw new ApiException(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID,
                    amountText(scanEntry.amount()), runningPending.toPlainString());
            }
        }
    }

    private String resolveSettlementState(BigDecimal issuedAmount, BigDecimal settledPlusAdjusted,
                                          BigDecimal pendingAmount, boolean hasEffectiveEntry) {
        if (issuedAmount.compareTo(ZERO) == 0) {
            return hasEffectiveEntry
                    ? FinanceOrderSettlementStateEnum.REVIEW_REQUIRED.getValue()
                    : FinanceOrderSettlementStateEnum.EMPTY.getValue();
        }
        if (settledPlusAdjusted.compareTo(ZERO) == 0) {
            return FinanceOrderSettlementStateEnum.UNSETTLED.getValue();
        }
        if (pendingAmount.compareTo(ZERO) > 0) {
            return FinanceOrderSettlementStateEnum.PARTIAL.getValue();
        }
        return FinanceOrderSettlementStateEnum.SETTLED.getValue();
    }

    private String amountText(BigDecimal amount) {
        return Objects.isNull(amount) ? "null" : amount.toPlainString();
    }

    /**
     * 计算过程累计器
     */
    private static class Ledger {
        private BigDecimal outAmount = ZERO;
        private BigDecimal inAmount = ZERO;
        private BigDecimal adjustedAmount = ZERO;
        private BigDecimal interestIncome = ZERO;
        private BigDecimal interestExpense = ZERO;
        private BigDecimal feeAmount = ZERO;
        private int pendingEntryCount;
        private boolean hasEffectiveEntry;
        private boolean hasEffectiveInterestOrFee;
        private final List<ScanEntry> scanEntries = new ArrayList<>();
        private int scanIndex;

        private int nextScanIndex() {
            return scanIndex++;
        }
    }

    /**
     * 时间前缀校验记录
     */
    private record ScanEntry(Long billId, BigDecimal amount, BigDecimal delta,
                             LocalDateTime tradeTime, int originalIndex) {
    }
}
