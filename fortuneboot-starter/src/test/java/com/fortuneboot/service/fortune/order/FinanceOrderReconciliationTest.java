package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderReconciliationVo;
import com.fortuneboot.orderapi.FinanceOrderStage04TestApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("历史单据核对、安全回填与报告")
@SpringBootTest(classes = FinanceOrderStage04TestApplication.class)
class FinanceOrderReconciliationTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage04-reconcile-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FinanceOrderReconciliationService reconciliationService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FinanceOrderStage03TestSupport.Stage03Fixture fixture;

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderStage03TestSupport.cleanAllTables(jdbcTemplate);
        this.fixture = FinanceOrderStage03TestSupport.insertFixture(jdbcTemplate);
        insertProblemHistory();
    }

    @Test
    @DisplayName("安全1型7/8回填重建缓存但不改余额且可幂等")
    void safeBackfill_rebuildsCacheWithoutBalanceChangeAndIsIdempotent() {
        String accountABefore = balanceText(fixture.accountIdA());
        String accountBBefore = balanceText(fixture.accountIdB());
        String usdBefore = balanceText(1003L);

        List<FinanceOrderReconciliationVo> afterFirst = reconciliationService.backfillSafe(fixture.bookIdA());
        List<FinanceOrderReconciliationVo> afterSecond = reconciliationService.backfillSafe(fixture.bookIdA());

        assertThat(orderStatus(1L)).isEqualTo("READY");
        assertThat(orderVersion(1L)).isEqualTo(1L);
        assertThat(orderAmount(1L, "out_amount")).isEqualByComparingTo("100.00");
        assertThat(orderAmount(1L, "in_amount")).isEqualByComparingTo("50.00");
        assertThat(billLedger(1L)).isEqualTo(1);
        assertThat(billComponent(1L)).isEqualTo("PRINCIPAL");
        assertThat(balanceText(fixture.accountIdA())).isEqualTo(accountABefore);
        assertThat(balanceText(fixture.accountIdB())).isEqualTo(accountBBefore);
        assertThat(balanceText(1003L)).isEqualTo(usdBefore);
        assertThat(afterSecond).isEqualTo(afterFirst);

        reconciliationService.backfillSafe(fixture.bookIdA());
        assertThat(orderVersion(1L)).isEqualTo(1L);
        assertThat(auditCount(1L, "RECONCILE")).isEqualTo(1);
    }

    @Test
    @DisplayName("历史借贷、关闭欠款、混币种和孤立流水保持隔离并进入报告")
    void unsafeHistory_staysReviewRequiredAndIsReported() {
        reconciliationService.backfillSafe(fixture.bookIdA());
        List<FinanceOrderReconciliationVo> reports = reconciliationService.preview(fixture.bookIdA());

        assertThat(orderStatus(2L)).isEqualTo("REVIEW_REQUIRED");
        assertThat(billLedger(3L)).isZero();
        assertThat(orderStatus(3L)).isEqualTo("REVIEW_REQUIRED");
        assertThat(orderStatus(4L)).isEqualTo("REVIEW_REQUIRED");
        assertThat(reports).extracting(FinanceOrderReconciliationVo::getOrderId)
                .containsExactlyInAnyOrder(2L, 3L, 4L, null);
        assertThat(reports).anySatisfy(report -> {
            assertThat(report.getOrderId()).isEqualTo(2L);
            assertThat(report.getIssueCodes()).contains("LEGACY_LOAN_ISOLATED");
            assertThat(report.isCashEffectKnown()).isFalse();
        });
        assertThat(reports).anySatisfy(report -> {
            assertThat(report.getOrderId()).isEqualTo(3L);
            assertThat(report.getIssueCodes()).contains("CLOSED_NOT_SETTLED", "ENTRY_AFTER_CLOSE");
        });
        assertThat(reports).anySatisfy(report -> {
            assertThat(report.getOrderId()).isEqualTo(4L);
            assertThat(report.getIssueCodes()).contains("CURRENCY_MISMATCH");
            assertThat(report.getCurrencyCodes()).contains("CNY", "USD");
        });

        PageDTO<FinanceOrderReconciliationVo> page = reconciliationService.page(fixture.bookIdA(), 1, 3);
        assertThat(page.getRows()).hasSize(3);
        assertThat(page.getTotal()).isEqualTo(4L);
    }

    @Test
    @DisplayName("孤立9—12流水单列报告且不改变账户余额")
    void orphanLegacyBills_areReportedWithoutBalanceChange() {
        String before = balanceText(fixture.accountIdA());
        List<FinanceOrderReconciliationVo> reports = reconciliationService.backfillSafe(fixture.bookIdA());

        assertThat(reports).anySatisfy(report -> {
            assertThat(report.getOrderId()).isNull();
            assertThat(report.getBillId()).isEqualTo(6L);
            assertThat(report.getIssueCodes()).contains("ORPHAN_ORDER");
        });
        assertThat(balanceText(fixture.accountIdA())).isEqualTo(before);
    }

    @Test
    @DisplayName("报告CSV使用UTF-8转义并阻止Excel公式注入")
    void csvExport_isUtf8Rfc4180AndFormulaSafe() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        reconciliationService.writeCsv(fixture.bookIdA(), outputStream);
        String csv = outputStream.toString(StandardCharsets.UTF_8);

        assertThat(csv).startsWith("﻿orderId,billId,bookId,issueCodes,originalStatus,originalOutAmount,"
                + "originalInAmount,candidateIssuedAmount,candidateSettledAmount,candidatePendingAmount,"
                + "accountIds,currencyCodes,cashEffectKnown,note,generatedAt");
        assertThat(csv).contains("\n\"2\",\"\"");
        assertThat(csv).contains("\n\"\",\"6\"");
        assertThat(csv).contains("=1+1");
        assertThat(csv).contains("\"'=1+1");
        assertThat(csv).contains("\"LEGACY_LOAN_ISOLATED\"");
    }

    private void insertProblemHistory() {
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (1003, 900, '美元账户', 1, 7000.00, 'USD', 1, 1, 1, 1, 0)
                """);
        insertOrder(1L, 1, 200, null, "安全历史报销", null);
        insertLegacyBill(1L, 1L, 7, "100.00", fixture.accountIdA(), "CNY", "2026-10-01 09:00:00");
        insertLegacyBill(2L, 1L, 8, "50.00", fixture.accountIdA(), "CNY", "2026-10-02 09:00:00");

        insertOrder(2L, 2, 200, null, "历史债权", null);
        insertLegacyBill(3L, 2L, 9, "100.00", fixture.accountIdA(), "CNY", "2026-10-01 09:00:00");

        insertOrder(3L, 1, 1000, "2026-10-02 20:00:00", "关闭欠款", "=1+1");
        insertLegacyBill(4L, 3L, 7, "100.00", fixture.accountIdA(), "CNY", "2026-10-03 09:00:00");

        insertOrder(4L, 1, 200, null, "混币种报销", null);
        insertLegacyBill(5L, 4L, 7, "80.00", 1003L, "USD", "2026-10-01 09:00:00");

        insertLegacyBill(6L, null, 9, "30.00", fixture.accountIdA(), "CNY", "2026-10-01 09:00:00");
        jdbcTemplate.update("update fortune_bill set order_id = 999 where bill_id = 6");
    }

    private void insertOrder(Long orderId, Integer type, Integer status, String closeTime, String title,
                             String reconciliationNote) {
        jdbcTemplate.update("""
                insert into fortune_finance_order(order_id,book_id,title,type,status,submit_time,close_time,
                    currency_code,version,reconciliation_status,reconciliation_note,adjusted_amount,deleted)
                values (?,?,?, ?, ?, '2026-10-01 08:00:00', ?, 'CNY', 0, 'REVIEW_REQUIRED', ?, 0, 0)
                """, orderId, fixture.bookIdA(), title, type, status, closeTime, reconciliationNote);
    }

    private void insertLegacyBill(Long billId, Long orderId, Integer billType, String amount, Long accountId,
                                  String currency, String tradeTime) {
        jdbcTemplate.update("""
                insert into fortune_bill(bill_id,book_id,title,trade_time,account_id,amount,converted_amount,
                    order_id,bill_type,confirm,include,recycle_bin,deleted)
                values (?,?,?, ?, ?, ?, ?, ?, ?, 1, 1, 0, 0)
                """, billId, fixture.bookIdA(), "历史流水" + billId, tradeTime, accountId,
                new BigDecimal(amount), new BigDecimal(amount), orderId, billType);
    }

    private String orderStatus(Long orderId) {
        return jdbcTemplate.queryForObject(
                "select reconciliation_status from fortune_finance_order where order_id = ?", String.class, orderId);
    }

    private Long orderVersion(Long orderId) {
        return jdbcTemplate.queryForObject(
                "select version from fortune_finance_order where order_id = ?", Long.class, orderId);
    }

    private BigDecimal orderAmount(Long orderId, String column) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from fortune_finance_order where order_id = ?", BigDecimal.class, orderId);
    }

    private Integer billLedger(Long billId) {
        return jdbcTemplate.queryForObject(
                "select order_ledger_version from fortune_bill where bill_id = ?", Integer.class, billId);
    }

    private String billComponent(Long billId) {
        return jdbcTemplate.queryForObject(
                "select order_component from fortune_bill where bill_id = ?", String.class, billId);
    }

    private String balanceText(Long accountId) {
        return jdbcTemplate.queryForObject(
                "select cast(balance as text) from fortune_account where account_id = ?", String.class, accountId);
    }

    private int auditCount(Long orderId, String action) {
        return jdbcTemplate.queryForObject("""
                select count(*) from fortune_finance_order_audit
                where order_id = ? and action = ?
                """, Integer.class, orderId, action);
    }
}
