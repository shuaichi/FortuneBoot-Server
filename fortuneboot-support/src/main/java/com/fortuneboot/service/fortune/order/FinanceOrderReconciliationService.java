package com.fortuneboot.service.fortune.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationIssueEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageUtil;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderReconciliationVo;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 历史单据核对服务。
 * <p>只读扫描所有历史；safe-backfill仅重建可证明安全的1型7/8汇总缓存，不回放现金。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderReconciliationService {

    private static final DateTimeFormatter GENERATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FortuneAccountRepo accountRepo;
    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FortuneFinanceOrderAuditRepo auditRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderScopeLock scopeLock;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public List<FinanceOrderReconciliationVo> preview(Long bookId) {
        return buildReports(scan(bookId));
    }

    public List<FinanceOrderReconciliationVo> backfillSafe(Long bookId) {
        Scan scan = scan(bookId);
        for (OrderScan orderScan : scan.orders()) {
            if (!orderScan.safe()) {
                continue;
            }
            try {
                transactionTemplate.executeWithoutResult(status -> applySafeBackfill(orderScan));
            } catch (RuntimeException exception) {
                orderScan.issueCodes().add(FinanceOrderReconciliationIssueEnum.BACKFILL_FAILED.name());
                orderScan.note(orderScan.note() == null ? exception.getMessage()
                        : orderScan.note() + "; " + exception.getMessage());
                orderScan.safe(false);
            }
        }
        return buildReports(scan);
    }

    public PageDTO<FinanceOrderReconciliationVo> page(Long bookId, Integer pageNum, Integer pageSize) {
        List<FinanceOrderReconciliationVo> reports = preview(bookId);
        return new PageDTO<>(FinanceOrderPageUtil.paginate(reports, pageNum, pageSize), (long) reports.size());
    }

    public void writeCsv(Long bookId, OutputStream outputStream) {
        List<FinanceOrderReconciliationVo> reports = preview(bookId);
        String generatedAt = GENERATED_AT_FORMAT.format(LocalDateTime.now(clock));
        StringBuilder builder = new StringBuilder();
        builder.append("orderId,billId,bookId,issueCodes,originalStatus,originalOutAmount,")
                .append("originalInAmount,candidateIssuedAmount,candidateSettledAmount,")
                .append("candidatePendingAmount,accountIds,currencyCodes,cashEffectKnown,note,generatedAt\n");
        for (FinanceOrderReconciliationVo report : reports) {
            builder.append(csvCell(report.getOrderId() == null ? null : String.valueOf(report.getOrderId()), false)).append(',')
                    .append(csvCell(report.getBillId() == null ? null : String.valueOf(report.getBillId()), false)).append(',')
                    .append(csvCell(report.getBookId() == null ? null : String.valueOf(report.getBookId()), false)).append(',')
                    .append(csvCell(String.join("|", nullSafe(report.getIssueCodes())), false)).append(',')
                    .append(csvCell(report.getOriginalStatus() == null ? null : String.valueOf(report.getOriginalStatus()), false)).append(',')
                    .append(csvCell(report.getOriginalOutAmount(), false)).append(',')
                    .append(csvCell(report.getOriginalInAmount(), false)).append(',')
                    .append(csvCell(report.getCandidateIssuedAmount(), false)).append(',')
                    .append(csvCell(report.getCandidateSettledAmount(), false)).append(',')
                    .append(csvCell(report.getCandidatePendingAmount(), false)).append(',')
                    .append(csvCell(join(report.getAccountIds()), false)).append(',')
                    .append(csvCell(String.join("|", nullSafe(report.getCurrencyCodes())), false)).append(',')
                    .append(csvCell(String.valueOf(report.isCashEffectKnown()), false)).append(',')
                    .append(csvCell(report.getNote(), true)).append(',')
                    .append(csvCell(generatedAt, false))
                    .append('\n');
        }
        try {
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);
            outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("单据核对报告CSV写入失败", exception);
        }
    }

    private Scan scan(Long bookId) {
        FortuneBookEntity book = accessGuard.loadBook(bookId);
        List<FortuneFinanceOrderEntity> orders = orderRepo.listByBookIdIncludeDeleted(bookId);
        List<FortuneBillEntity> bills = billRepo.listByBookIdIncludeDeleted(bookId);
        Map<Long, List<FortuneBillEntity>> billsByOrder = bills.stream()
                .filter(bill -> bill.getOrderId() != null)
                .collect(Collectors.groupingBy(FortuneBillEntity::getOrderId, TreeMap::new, Collectors.toList()));
        Set<Long> accountIds = bills.stream()
                .flatMap(bill -> java.util.stream.Stream.of(bill.getAccountId(), bill.getToAccountId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, FortuneAccountEntity> accountMap = accountIds.isEmpty() ? Map.of()
                : accountRepo.listByIds(accountIds).stream()
                .collect(Collectors.toMap(FortuneAccountEntity::getAccountId, account -> account));

        List<OrderScan> orderScans = new ArrayList<>();
        for (FortuneFinanceOrderEntity order : orders) {
            if (Boolean.TRUE.equals(order.getDeleted())) {
                continue;
            }
            orderScans.add(scanOrder(book, order, billsByOrder.getOrDefault(order.getOrderId(), List.of()), accountMap));
        }
        return new Scan(orderScans, orphanReports(bookId, bills, orders));
    }

    private OrderScan scanOrder(FortuneBookEntity book, FortuneFinanceOrderEntity order,
                                List<FortuneBillEntity> bills, Map<Long, FortuneAccountEntity> accountMap) {
        LinkedHashSet<String> issues = new LinkedHashSet<>();
        LinkedHashSet<Long> accountIds = new LinkedHashSet<>();
        LinkedHashSet<String> currencies = new LinkedHashSet<>();
        boolean cashEffectKnown = true;
        FinanceOrderTypeEnum orderType = orderType(order.getType());
        if (orderType == null) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_ORDER_TYPE.name());
            cashEffectKnown = false;
        }
        if (!statusValid(order.getStatus())) {
            issues.add(FinanceOrderReconciliationIssueEnum.UNKNOWN_STATUS.name());
            cashEffectKnown = false;
        }
        String expectedCurrency = order.getCurrencyCode() == null ? book.getDefaultCurrency() : order.getCurrencyCode();
        currencies.add(expectedCurrency);

        List<FinanceOrderLedgerEntry> candidateEntries = new ArrayList<>();
        for (FortuneBillEntity bill : bills) {
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
                    candidateEntries.add(new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(),
                            FinanceOrderComponentEnum.PRINCIPAL.getValue(), bill.getAmount(), bill.getTradeTime(),
                            Boolean.TRUE.equals(bill.getConfirm()), Boolean.TRUE.equals(bill.getRecycleBin()),
                            Boolean.TRUE.equals(bill.getDeleted()), FinanceOrderLedgerVersionEnum.CURRENT.getValue()));
                } else if (FinanceOrderLedgerVersionEnum.isCurrentValue(bill.getOrderLedgerVersion())) {
                    candidateEntries.add(ledgerEntry(bill));
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
                    candidateEntries.add(ledgerEntry(bill));
                }
            }
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
        return new OrderScan(order, bills, issues, accountIds, currencies, snapshot, cashEffectKnown, safe, note);
    }

    private List<OrphanScan> orphanReports(Long bookId, List<FortuneBillEntity> bills,
                                          List<FortuneFinanceOrderEntity> orders) {
        Set<Long> existingOrderIds = orders.stream()
                .filter(order -> !Boolean.TRUE.equals(order.getDeleted()))
                .map(FortuneFinanceOrderEntity::getOrderId)
                .collect(Collectors.toSet());
        List<OrphanScan> result = new ArrayList<>();
        for (FortuneBillEntity bill : bills) {
            if (bill.getOrderId() == null || existingOrderIds.contains(bill.getOrderId())) {
                continue;
            }
            result.add(new OrphanScan(bill,
                    Set.of(FinanceOrderReconciliationIssueEnum.ORPHAN_ORDER.name()), bookId));
        }
        return result;
    }

    private void applySafeBackfill(OrderScan orderScan) {
        FortuneFinanceOrderEntity before = orderScan.order();
        scopeLock.lockGroup(before.getBookId() == null ? null : accessGuard.loadBook(before.getBookId()).getGroupId());
        FortuneFinanceOrderEntity order = orderRepo.getById(before.getOrderId());
        if (order == null || !Objects.equals(order.getBookId(), before.getBookId())
                || !FinanceOrderReconciliationStatusEnum.isReviewRequiredValue(order.getReconciliationStatus())) {
            return;
        }
        long expectedVersion = order.getVersion();
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        order.setVersion(expectedVersion + 1);
        Map<String, Object> beforeJson = auditSnapshot(order);

        if (order.getCurrencyCode() == null) {
            order.setCurrencyCode(accessGuard.loadBook(order.getBookId()).getDefaultCurrency());
        }
        if (order.getCounterpartyName() == null || order.getCounterpartyName().isBlank()) {
            order.setCounterpartyName("未填写");
        }
        FinanceOrderLedgerSnapshot snapshot = orderScan.snapshot();
        if (snapshot != null) {
            order.setOutAmount(snapshot.outAmount());
            order.setInAmount(snapshot.inAmount());
            order.setAdjustedAmount(snapshot.adjustedAmount());
            for (FortuneBillEntity bill : orderScan.bills()) {
                if (FinanceOrderLedgerVersionEnum.isLegacyValue(bill.getOrderLedgerVersion())
                        && BillTypeEnum.isClaimValue(bill.getBillType())) {
                    bill.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
                    bill.setOrderLedgerVersion(FinanceOrderLedgerVersionEnum.CURRENT.getValue());
                    billRepo.updateById(bill);
                }
            }
        }
        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.READY.getValue());
        order.setReconciliationNote(null);
        orderRepo.updateById(order);

        FortuneFinanceOrderAuditEntity audit = new FortuneFinanceOrderAuditEntity();
        audit.setBookId(order.getBookId());
        audit.setOrderId(order.getOrderId());
        audit.setAction(FinanceOrderAuditActionEnum.RECONCILE.name());
        audit.setBeforeJson(writeJson(beforeJson));
        audit.setAfterJson(writeJson(auditSnapshot(order)));
        auditRepo.save(audit);
    }

    private List<FinanceOrderReconciliationVo> buildReports(Scan scan) {
        List<FinanceOrderReconciliationVo> result = new ArrayList<>();
        for (OrderScan orderScan : scan.orders()) {
            if (!orderScan.safe() && !orderScan.issueCodes().isEmpty()) {
                result.add(orderReport(orderScan));
            }
        }
        scan.orphans().stream()
                .sorted(Comparator.comparing(orphan -> orphan.bill().getBillId()))
                .forEach(orphan -> result.add(orphanReport(orphan)));
        return result.stream()
                .sorted(Comparator.comparing(FinanceOrderReconciliationVo::getOrderId,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(FinanceOrderReconciliationVo::getBillId,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private FinanceOrderReconciliationVo orderReport(OrderScan orderScan) {
        FinanceOrderReconciliationVo vo = baseReport(orderScan.order().getBookId(), orderScan.issueCodes(),
                orderScan.currencies());
        vo.setOrderId(orderScan.order().getOrderId());
        vo.setOriginalStatus(orderScan.order().getStatus());
        vo.setOriginalOutAmount(amount(orderScan.order().getOutAmount()));
        vo.setOriginalInAmount(amount(orderScan.order().getInAmount()));
        FinanceOrderLedgerSnapshot snapshot = orderScan.snapshot();
        if (snapshot != null) {
            vo.setCandidateIssuedAmount(amount(snapshot.issuedAmount()));
            vo.setCandidateSettledAmount(amount(snapshot.settledAmount()));
            vo.setCandidatePendingAmount(amount(snapshot.pendingAmount()));
        }
        vo.setAccountIds(orderScan.accountIds().toArray(Long[]::new));
        vo.setCashEffectKnown(orderScan.cashEffectKnown());
        vo.setNote(orderScan.note());
        return vo;
    }

    private FinanceOrderReconciliationVo orphanReport(OrphanScan orphan) {
        FinanceOrderReconciliationVo vo = baseReport(orphan.bookId(), orphan.issueCodes(), Set.of());
        vo.setBillId(orphan.bill().getBillId());
        vo.setAccountIds(orphan.bill().getAccountId() == null && orphan.bill().getToAccountId() == null
                ? new Long[0] : new Long[]{orphan.bill().getAccountId()});
        vo.setNote("关联单据不存在或已删除");
        return vo;
    }

    private FinanceOrderReconciliationVo baseReport(Long bookId, Set<String> issueCodes,
                                                    Set<String> currencies) {
        FinanceOrderReconciliationVo vo = new FinanceOrderReconciliationVo();
        vo.setBookId(bookId);
        vo.setIssueCodes(issueCodes.toArray(String[]::new));
        vo.setCurrencyCodes(currencies.toArray(String[]::new));
        return vo;
    }

    private FinanceOrderLedgerEntry ledgerEntry(FortuneBillEntity bill) {
        return new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(), bill.getOrderComponent(),
                bill.getAmount(), bill.getTradeTime(),
                Boolean.TRUE.equals(bill.getConfirm()), Boolean.TRUE.equals(bill.getRecycleBin()),
                Boolean.TRUE.equals(bill.getDeleted()), FinanceOrderLedgerVersionEnum.CURRENT.getValue());
    }

    private boolean isKnownWriteOff(FortuneBillEntity bill) {
        return FinanceOrderComponentEnum.WRITE_OFF.getValue().equals(bill.getOrderComponent());
    }

    private boolean statusValid(Integer status) {
        return FinanceOrderStatusEnum.getByValue(status) != null;
    }

    private FinanceOrderTypeEnum orderType(Integer type) {
        return FinanceOrderTypeEnum.getByValue(type);
    }

    private String amount(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    private Map<String, Object> auditSnapshot(FortuneFinanceOrderEntity order) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", order.getOrderId());
        value.put("status", order.getStatus());
        value.put("currencyCode", order.getCurrencyCode());
        value.put("counterpartyName", order.getCounterpartyName());
        value.put("outAmount", order.getOutAmount());
        value.put("inAmount", order.getInAmount());
        value.put("adjustedAmount", order.getAdjustedAmount());
        value.put("version", order.getVersion());
        value.put("reconciliationStatus", order.getReconciliationStatus());
        return value;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("历史核对审计JSON序列化失败", exception);
        }
    }

    private String csvCell(String value, boolean alwaysFormulaSafe) {
        String text = value == null ? "" : value;
        if (alwaysFormulaSafe || !text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String join(Long[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return java.util.Arrays.stream(values).map(String::valueOf).collect(Collectors.joining("|"));
    }

    private List<String> nullSafe(String[] values) {
        return values == null ? List.of() : List.of(values);
    }

    /**
     * 只读扫描结果
     */
    private record Scan(List<OrderScan> orders, List<OrphanScan> orphans) {
    }

    /**
     * 单笔单据扫描结果
     */
    private static final class OrderScan {
        private final FortuneFinanceOrderEntity order;
        private final List<FortuneBillEntity> bills;
        private final LinkedHashSet<String> issueCodes;
        private final LinkedHashSet<Long> accountIds;
        private final LinkedHashSet<String> currencies;
        private final FinanceOrderLedgerSnapshot snapshot;
        private boolean cashEffectKnown;
        private boolean safe;
        private String note;

        private OrderScan(FortuneFinanceOrderEntity order, List<FortuneBillEntity> bills,
                           LinkedHashSet<String> issueCodes, LinkedHashSet<Long> accountIds,
                           LinkedHashSet<String> currencies, FinanceOrderLedgerSnapshot snapshot,
                           boolean cashEffectKnown, boolean safe, String note) {
            this.order = order;
            this.bills = bills;
            this.issueCodes = issueCodes;
            this.accountIds = accountIds;
            this.currencies = currencies;
            this.snapshot = snapshot;
            this.cashEffectKnown = cashEffectKnown;
            this.safe = safe;
            this.note = note;
        }

        private FortuneFinanceOrderEntity order() {
            return order;
        }

        private List<FortuneBillEntity> bills() {
            return bills;
        }

        private LinkedHashSet<String> issueCodes() {
            return issueCodes;
        }

        private LinkedHashSet<Long> accountIds() {
            return accountIds;
        }

        private LinkedHashSet<String> currencies() {
            return currencies;
        }

        private FinanceOrderLedgerSnapshot snapshot() {
            return snapshot;
        }

        private boolean cashEffectKnown() {
            return cashEffectKnown;
        }

        private boolean safe() {
            return safe;
        }

        private void safe(boolean value) {
            this.safe = value;
        }

        private String note() {
            return note;
        }

        private void note(String value) {
            this.note = value;
        }
    }

    /**
     * 孤立流水扫描结果
     */
    private record OrphanScan(FortuneBillEntity bill, Set<String> issueCodes, Long bookId) {
    }
}
