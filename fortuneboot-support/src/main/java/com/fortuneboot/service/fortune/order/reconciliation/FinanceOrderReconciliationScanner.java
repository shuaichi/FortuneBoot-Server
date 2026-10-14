package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationIssueEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.service.fortune.order.FinanceOrderAccessGuard;
import com.fortuneboot.service.fortune.order.FinanceOrderAmountRule;
import com.fortuneboot.service.fortune.order.FinanceOrderLedgerCalculator;
import com.fortuneboot.service.fortune.order.FinanceOrderLedgerEntryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 历史单据核对扫描器。
 * <p>只读加载账本全部历史并识别问题，不执行任何写入。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderReconciliationScanner {

    private final FortuneAccountRepo accountRepo;
    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final FinanceOrderLedgerEntryFactory ledgerEntryFactory;

    public FinanceOrderReconciliationScan scan(Long bookId) {
        FortuneBookEntity book = accessGuard.loadBook(bookId);
        List<FortuneFinanceOrderEntity> orders = orderRepo.listByBookIdIncludeDeleted(bookId);
        List<FortuneBillEntity> bills = billRepo.listByBookIdIncludeDeleted(bookId);
        Map<Long, List<FortuneBillEntity>> billsByOrder = bills.stream()
                .filter(bill -> bill.getOrderId() != null)
                .collect(Collectors.groupingBy(FortuneBillEntity::getOrderId, TreeMap::new, Collectors.toList()));
        Set<Long> accountIds = bills.stream()
                .flatMap(bill -> Stream.of(bill.getAccountId(), bill.getToAccountId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, FortuneAccountEntity> accountMap = accountIds.isEmpty() ? Map.of()
                : accountRepo.listByIds(accountIds).stream()
                .collect(Collectors.toMap(FortuneAccountEntity::getAccountId, account -> account));

        List<FinanceOrderReconciliationOrderScan> orderScans = new ArrayList<>();
        for (FortuneFinanceOrderEntity order : orders) {
            if (Boolean.TRUE.equals(order.getDeleted())) {
                continue;
            }
            orderScans.add(scanOrder(book, order,
                    billsByOrder.getOrDefault(order.getOrderId(), List.of()), accountMap));
        }
        return new FinanceOrderReconciliationScan(orderScans, orphanScans(bookId, bills, orders));
    }

    private FinanceOrderReconciliationOrderScan scanOrder(FortuneBookEntity book,
                                                           FortuneFinanceOrderEntity order,
                                                           List<FortuneBillEntity> bills,
                                                           Map<Long, FortuneAccountEntity> accountMap) {
        LinkedHashSet<String> issues = new LinkedHashSet<>();
        LinkedHashSet<Long> accountIds = new LinkedHashSet<>();
        LinkedHashSet<String> currencies = new LinkedHashSet<>();
        boolean cashEffectKnown = true;
        FinanceOrderTypeEnum orderType = FinanceOrderTypeEnum.getByValue(order.getType());
        if (orderType == null) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_ORDER_TYPE.name());
            cashEffectKnown = false;
        }
        if (!statusValid(order.getStatus())) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_STATUS.name());
            cashEffectKnown = false;
        }
        String expectedCurrency = order.getCurrencyCode() == null
                ? book.getDefaultCurrency() : order.getCurrencyCode();
        currencies.add(expectedCurrency);

        List<FinanceOrderLedgerEntry> candidateEntries = new ArrayList<>();
        for (FortuneBillEntity bill : bills) {
            cashEffectKnown = scanBill(order, orderType, bill, accountMap, expectedCurrency,
                    issues, accountIds, currencies, candidateEntries) && cashEffectKnown;
        }

        FinanceOrderLedgerSnapshot snapshot = null;
        if (orderType != null && issues.stream().noneMatch(code ->
                code.equals(FinanceOrderReconciliationIssueEnum.UNKNOWN_BILL_TYPE.name())
                        || code.equals(FinanceOrderReconciliationIssueEnum.UNKNOWN_COMPONENT.name())
                        || code.equals(FinanceOrderReconciliationIssueEnum.UNKNOWN_LEDGER_VERSION.name()))) {
            try {
                snapshot = ledgerCalculator.calculate(order.getType(), candidateEntries);
            } catch (ApiException exception) {
                issues.add(FinanceOrderReconciliationIssueEnum.CALCULATION_INVALID.name());
                cashEffectKnown = false;
            }
        }
        if (snapshot != null) {
            if (Objects.equals(order.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue())) {
                if (snapshot.pendingAmount().compareTo(BigDecimal.ZERO) != 0) {
                    issues.add(FinanceOrderReconciliationIssueEnum.CLOSED_NOT_SETTLED.name());
                    cashEffectKnown = false;
                }
                if (order.getCloseTime() != null && bills.stream().anyMatch(bill ->
                        bill.getTradeTime() != null && bill.getTradeTime().isAfter(order.getCloseTime()))) {
                    issues.add(FinanceOrderReconciliationIssueEnum.ENTRY_AFTER_CLOSE.name());
                    cashEffectKnown = false;
                }
            }
        }
        if (bills.isEmpty() && orderType != null && statusValid(order.getStatus())) {
            cashEffectKnown = true;
        }

        boolean reviewRequired = FinanceOrderReconciliationStatusEnum
                .isReviewRequiredValue(order.getReconciliationStatus());
        boolean safe = reviewRequired && issues.isEmpty() && snapshot != null
                && (bills.isEmpty() || orderType == FinanceOrderTypeEnum.EXPENSE_CLAIM);
        String issueText = issues.isEmpty() ? null : String.join(";", issues);
        String note = issueText == null ? order.getReconciliationNote()
                : order.getReconciliationNote() == null || order.getReconciliationNote().isBlank()
                ? issueText : order.getReconciliationNote() + ";" + issueText;
        return new FinanceOrderReconciliationOrderScan(order, bills, issues, accountIds, currencies,
                snapshot, cashEffectKnown, safe, note);
    }

    private boolean scanBill(FortuneFinanceOrderEntity order, FinanceOrderTypeEnum orderType,
                             FortuneBillEntity bill, Map<Long, FortuneAccountEntity> accountMap,
                             String expectedCurrency, LinkedHashSet<String> issues,
                             LinkedHashSet<Long> accountIds, LinkedHashSet<String> currencies,
                             List<FinanceOrderLedgerEntry> candidateEntries) {
        boolean cashEffectKnown = true;
        if (!Objects.equals(bill.getBookId(), order.getBookId())) {
            issues.add(FinanceOrderReconciliationIssueEnum.CROSS_BOOK.name());
            cashEffectKnown = false;
        }
        if (FinanceOrderLedgerVersionEnum.getByValue(bill.getOrderLedgerVersion()) == null) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_LEDGER_VERSION.name());
            cashEffectKnown = false;
        }
        FinanceOrderComponentEnum component = FinanceOrderComponentEnum.getByValue(bill.getOrderComponent());
        if (FinanceOrderLedgerVersionEnum.isCurrentValue(bill.getOrderLedgerVersion()) && component == null) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_COMPONENT.name());
            cashEffectKnown = false;
        }
        if (bill.getBillType() == null || FinanceOrderComponentEnum.getByValue(bill.getOrderComponent()) == null
                && !FinanceOrderLedgerVersionEnum.isLegacyValue(bill.getOrderLedgerVersion())) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_BILL_TYPE.name());
            cashEffectKnown = false;
        }
        if (!FinanceOrderAmountRule.isValid(bill.getAmount())) {
            issues.add(FinanceOrderReconciliationIssueEnum.AMOUNT_INVALID.name());
            cashEffectKnown = false;
        }
        if (bill.getTradeTime() == null) {
            issues.add(FinanceOrderReconciliationIssueEnum.TRADE_TIME_MISSING.name());
            cashEffectKnown = false;
        }
        if (bill.getToAccountId() != null) {
            issues.add(FinanceOrderReconciliationIssueEnum.TRANSFER_ACCOUNT_INVALID.name());
            cashEffectKnown = false;
        }
        if (bill.getAccountId() != null) {
            accountIds.add(bill.getAccountId());
            FortuneAccountEntity account = accountMap.get(bill.getAccountId());
            if (account == null) {
                issues.add(FinanceOrderReconciliationIssueEnum.ACCOUNT_MISSING.name());
                cashEffectKnown = false;
            } else {
                currencies.add(account.getCurrencyCode());
                if (!Objects.equals(account.getCurrencyCode(), expectedCurrency)) {
                    issues.add(FinanceOrderReconciliationIssueEnum.CURRENCY_MISMATCH.name());
                    cashEffectKnown = false;
                }
            }
        } else if (!isKnownWriteOff(bill)) {
            issues.add(FinanceOrderReconciliationIssueEnum.ACCOUNT_MISSING.name());
            cashEffectKnown = false;
        }

        if (orderType == FinanceOrderTypeEnum.EXPENSE_CLAIM) {
            if (FinanceOrderLedgerVersionEnum.isLegacyValue(bill.getOrderLedgerVersion())
                    && BillTypeEnum.isClaimValue(bill.getBillType())) {
                candidateEntries.add(ledgerEntryFactory.fromLegacyClaim(bill));
            } else if (FinanceOrderLedgerVersionEnum.isCurrentValue(bill.getOrderLedgerVersion())) {
                candidateEntries.add(ledgerEntryFactory.fromCurrent(bill));
            } else {
                issues.add(FinanceOrderReconciliationIssueEnum.CLAIM_LEGACY_TYPE_UNKNOWN.name());
                cashEffectKnown = false;
            }
        } else if (orderType == FinanceOrderTypeEnum.LOAN_OUT
                || orderType == FinanceOrderTypeEnum.LOAN_IN) {
            if (FinanceOrderLedgerVersionEnum.isLegacyValue(bill.getOrderLedgerVersion())) {
                issues.add(FinanceOrderReconciliationIssueEnum.LEGACY_LOAN_ISOLATED.name());
                cashEffectKnown = false;
            } else {
                candidateEntries.add(ledgerEntryFactory.fromCurrent(bill));
            }
        }
        return cashEffectKnown;
    }

    private List<FinanceOrderReconciliationOrphanScan> orphanScans(Long bookId, List<FortuneBillEntity> bills,
                                                                   List<FortuneFinanceOrderEntity> orders) {
        Set<Long> existingOrderIds = orders.stream()
                .filter(order -> !Boolean.TRUE.equals(order.getDeleted()))
                .map(FortuneFinanceOrderEntity::getOrderId)
                .collect(Collectors.toSet());
        List<FinanceOrderReconciliationOrphanScan> result = new ArrayList<>();
        for (FortuneBillEntity bill : bills) {
            if (bill.getOrderId() == null || existingOrderIds.contains(bill.getOrderId())) {
                continue;
            }
            result.add(new FinanceOrderReconciliationOrphanScan(bill,
                    Set.of(FinanceOrderReconciliationIssueEnum.ORPHAN_ORDER.name()), bookId));
        }
        return result;
    }

    private boolean isKnownWriteOff(FortuneBillEntity bill) {
        return FinanceOrderComponentEnum.WRITE_OFF.getValue().equals(bill.getOrderComponent());
    }

    private boolean statusValid(Integer status) {
        return FinanceOrderStatusEnum.getByValue(status) != null;
    }
}
