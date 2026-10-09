package com.fortuneboot.service.fortune.order;

import com.fortuneboot.orderapi.FinanceOrderStage03TestApplication;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderOptionQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderEntryVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderSummaryVo;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据查询服务SQLite真实数据")
@SpringBootTest(classes = FinanceOrderStage03TestApplication.class)
class FinanceOrderQueryServiceTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage03-query-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FinanceOrderRequestService requestService;
    @Autowired
    private FinanceOrderQueryService queryService;
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
        login(fixture.ownerUserId());
    }

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("分页22条取两页无重复且总数正确")
    void pageSupportsTwentyTwoRecordsAcrossTwoPagesWithoutDuplicates() {
        for (int i = 1; i <= 22; i++) {
            requestService.create(createCommand("单据" + i, 2, null, ""));
        }

        FinanceOrderPageQuery firstPage = pageQuery();
        PageDTO<FinanceOrderDetailVo> first = queryService.page(firstPage);
        FinanceOrderPageQuery secondPage = pageQuery();
        secondPage.setPageNum(2);
        PageDTO<FinanceOrderDetailVo> second = queryService.page(secondPage);

        assertThat(first.getRows()).hasSize(20);
        assertThat(first.getTotal()).isEqualTo(22L);
        assertThat(second.getRows()).hasSize(2);
        assertThat(second.getTotal()).isEqualTo(22L);
        assertThat(first.getRows()).doesNotContainAnyElementsOf(second.getRows());
    }

    @Test
    @DisplayName("type/status/keyword/逾期/settlementState独立筛选且不跨账本泄漏")
    void pageFiltersAreIndependentAndBookScoped() {
        Long draft = requestService.create(createCommand("搜索关键字-草稿", 2, null, "")).getOrderId();
        Long overdue = requestService.create(createCommand("搜索关键字-逾期", 2, "2026-10-07", "")).getOrderId();
        Long settledLoan = requestService.create(createCommand("其他标题-结清", 2, null, "")).getOrderId();
        Long claim = requestService.create(createCommand("报销单", 1, null, "")).getOrderId();
        FinanceOrderCreateCommand otherBook = createCommand("搜索关键字-账本B", 2, null, "");
        otherBook.setBookId(fixture.bookIdB());
        requestService.create(otherBook);

        FinanceOrderMutationVo issued = addEntry(overdue, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(overdue, issued.getVersion(), 10, "600.00", fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo loanIssued = addEntry(settledLoan, 0L, 9, "1000.00", fixture.accountIdA(), true,
                UUID.randomUUID());
        addEntry(settledLoan, loanIssued.getVersion(), 10, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(claim, 0L, 7, "800.00", fixture.accountIdA(), true, UUID.randomUUID());

        FinanceOrderPageQuery draftQuery = pageQuery();
        draftQuery.setStatus(100);
        draftQuery.setType(2);
        PageDTO<FinanceOrderDetailVo> drafts = queryService.page(draftQuery);
        assertThat(drafts.getRows()).extracting(FinanceOrderDetailVo::getOrderId).containsExactly(draft);

        FinanceOrderPageQuery keywordQuery = pageQuery();
        keywordQuery.setKeyword("搜索关键字");
        PageDTO<FinanceOrderDetailVo> keyword = queryService.page(keywordQuery);
        assertThat(keyword.getRows()).extracting(FinanceOrderDetailVo::getOrderId)
                .containsExactlyInAnyOrder(draft, overdue);

        FinanceOrderPageQuery settledQuery = pageQuery();
        settledQuery.setSettlementState("SETTLED");
        PageDTO<FinanceOrderDetailVo> settledRows = queryService.page(settledQuery);
        assertThat(settledRows.getRows()).extracting(FinanceOrderDetailVo::getOrderId).containsExactly(settledLoan);

        FinanceOrderPageQuery overdueQuery = pageQuery();
        overdueQuery.setOverdueOnly(true);
        PageDTO<FinanceOrderDetailVo> overdueRows = queryService.page(overdueQuery);
        assertThat(overdueRows.getRows()).extracting(FinanceOrderDetailVo::getOrderId).containsExactly(overdue);
        assertThat(overdueRows.getRows().getFirst().isOverdue()).isTrue();

        FinanceOrderPageQuery notDueQuery = pageQuery();
        notDueQuery.setOverdueOnly(true);
        Long todayDue = requestService.create(createCommand("今日到期", 2, "2026-10-08", "")).getOrderId();
        addEntry(todayDue, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        PageDTO<FinanceOrderDetailVo> notDue = queryService.page(notDueQuery);
        assertThat(notDue.getRows()).extracting(FinanceOrderDetailVo::getOrderId).containsExactly(overdue);
    }

    @Test
    @DisplayName("summary全账本数据库聚合并统计待核对和孤立流水")
    void summaryAggregatesWholeBookWithIntegrityCounters() {
        Long first = requestService.create(createCommand("债权一", 2, null, "")).getOrderId();
        Long second = requestService.create(createCommand("债权二", 2, "2026-10-07", "")).getOrderId();
        Long payable = requestService.create(createCommand("债务一", 3, null, "")).getOrderId();

        FinanceOrderMutationVo firstIssued = addEntry(first, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(first, firstIssued.getVersion(), 10, "400.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(second, 0L, 9, "700.00", fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo payableIssued = addEntry(payable, 0L, 11, "300.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(payable, payableIssued.getVersion(), 12, "300.00", fixture.accountIdA(), true, UUID.randomUUID());

        requestService.create(createCommand("待核对", 2, null, ""));
        jdbcTemplate.update("update fortune_finance_order set reconciliation_status = 'REVIEW_REQUIRED' "
                + "where title = '待核对'");
        jdbcTemplate.update("""
                insert into fortune_bill(book_id, title, trade_time, account_id, amount, converted_amount, bill_type,
                    confirm, include, order_id, order_component, order_ledger_version, recycle_bin, deleted)
                values (?, '孤立流水', '2026-10-01 09:00:00', ?, 100.00, 100.00, 9, 1, 1, 999999, 'PRINCIPAL', 0, 0, 0)
                """, fixture.bookIdA(), fixture.accountIdA());

        FinanceOrderSummaryVo summary = queryService.summary(fixture.bookIdA());
        assertThat(summary.getCurrencyCode()).isEqualTo("CNY");
        assertThat(summary.getReceivableAmount()).isEqualTo("1300.00");
        assertThat(summary.getPayableAmount()).isEqualTo("0.00");
        assertThat(summary.getReimbursementAmount()).isEqualTo("0.00");
        assertThat(summary.getOverdueReceivableAmount()).isEqualTo("700.00");
        assertThat(summary.getReceivableCount()).isEqualTo(2);
        assertThat(summary.getPayableCount()).isZero();
        assertThat(summary.getReviewRequiredCount()).isEqualTo(1);
        assertThat(summary.getOrphanLegacyBillCount()).isEqualTo(1);
        assertThat(summary.getAsOfDate()).isEqualTo("2026-10-08");
    }

    @Test
    @DisplayName("明细entryStatus明确控制回收站：ACTIVE/VOIDED/ALL")
    void entriesEntryStatusControlsRecycleBin() {
        Long orderId = requestService.create(createCommand("明细单据", 2, null, "")).getOrderId();
        FinanceOrderMutationVo first = addEntry(orderId, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo second = addEntry(orderId, first.getVersion(), 10, "400.00",
                fixture.accountIdA(), true, UUID.randomUUID());
        requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(orderId)
                .billId(second.getBillId())
                .operation(FinanceOrderEntryMutation.Operation.VOID)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(second.getVersion())
                .build());

        assertThat(entries(orderId, FinanceOrderEntryQuery.EntryStatus.ACTIVE).getRows())
                .extracting(FinanceOrderEntryVo::getBillId).containsExactly(first.getBillId());
        assertThat(entries(orderId, FinanceOrderEntryQuery.EntryStatus.VOIDED).getRows())
                .extracting(FinanceOrderEntryVo::getBillId).containsExactly(second.getBillId());
        assertThat(entries(orderId, FinanceOrderEntryQuery.EntryStatus.ALL).getRows())
                .extracting(FinanceOrderEntryVo::getBillId)
                .containsExactlyInAnyOrder(first.getBillId(), second.getBillId());

        FinanceOrderEntryVo active = entries(orderId, FinanceOrderEntryQuery.EntryStatus.ACTIVE).getRows().getFirst();
        assertThat(active.getAmount()).isEqualTo("1000.00");
        assertThat(active.getOrderComponent()).isEqualTo("PRINCIPAL");
        assertThat(active.getOrderLedgerVersion()).isEqualTo(1);
        assertThat(active.getRecycleBin()).isFalse();
        assertThat(active.getAllowedActions()).contains("EDIT", "UNCONFIRM", "VOID", "COPY");

        FinanceOrderEntryVo voided = entries(orderId, FinanceOrderEntryQuery.EntryStatus.VOIDED).getRows().getFirst();
        assertThat(voided.getRecycleBin()).isTrue();
        assertThat(voided.getAllowedActions()).contains("RESTORE", "COPY");
        assertThat(voided.getAllowedActions()).doesNotContain("EDIT");
    }

    @Test
    @DisplayName("options按C08本金映射过滤单据")
    void optionsFollowPrincipalMapping() {
        Long draft = requestService.create(createCommand("草稿债权", 2, null, "")).getOrderId();
        Long using = requestService.create(createCommand("进行中债权", 2, null, "")).getOrderId();
        Long settled = requestService.create(createCommand("结清债权", 2, null, "")).getOrderId();
        Long claim = requestService.create(createCommand("报销单", 1, null, "")).getOrderId();

        addEntry(using, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo settledIssued = addEntry(settled, 0L, 9, "500.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(settled, settledIssued.getVersion(), 10, "500.00", fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo claimIssued = addEntry(claim, 0L, 7, "800.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(claim, claimIssued.getVersion(), 8, "300.00", fixture.accountIdA(), true, UUID.randomUUID());

        assertThat(optionOrderIds(9)).containsExactlyInAnyOrder(draft, using, settled);
        assertThat(optionOrderIds(10)).containsExactly(using);
        assertThat(optionOrderIds(8)).containsExactly(claim);
        assertThat(optionOrderIds(11)).isEmpty();
        assertThat(optionOrderIds(1)).isEmpty();
    }

    @Test
    @DisplayName("待核对单据detail保留原记录、金额null并给readOnlyReason")
    void detailForReviewRequiredOrderShowsReadOnlyReason() {
        Long orderId = requestService.create(createCommand("待核对象", 2, "2026-10-07", "原始备注")).getOrderId();
        addEntry(orderId, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        jdbcTemplate.update("""
                update fortune_finance_order
                set reconciliation_status = 'REVIEW_REQUIRED', reconciliation_note = '历史流水无法证明现金效果'
                where order_id = ?
                """, orderId);

        FinanceOrderDetailVo detail = queryService.detail(fixture.bookIdA(), orderId);
        assertThat(detail.getReconciliationStatus()).isEqualTo("REVIEW_REQUIRED");
        assertThat(detail.getSettlementState()).isEqualTo("REVIEW_REQUIRED");
        assertThat(detail.getReadOnlyReason()).isNotBlank();
        assertThat(detail.getTitle()).isEqualTo("待核对象");
        assertThat(detail.getRemark()).isEqualTo("原始备注");
        assertThat(detail.getPendingAmount()).isNull();
        assertThat(detail.getIssuedAmount()).isNull();
        assertThat(detail.isCanEdit()).isFalse();
        assertThat(detail.isCanClose()).isFalse();
        assertThat(detail.isCanDelete()).isFalse();
        assertThat(detail.isCanReopen()).isFalse();
        assertThat(detail.getAllowedEntryActions()).isEmpty();

        assertThatThrownBy(() -> queryService.detail(fixture.bookIdB(), orderId))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_BOOK_NOT_MATCH));
    }

    @Test
    @DisplayName("审计分页按时间倒序且记录生命周期动作")
    void auditPagesLifecycleActionsNewestFirst() {
        Long orderId = requestService.create(createCommand("审计单据", 2, null, "")).getOrderId();
        FinanceOrderMutationVo added = addEntry(orderId, 0L, 9, "1000.00", fixture.accountIdA(), true,
                UUID.randomUUID());
        addEntry(orderId, added.getVersion(), 10, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());

        FinanceOrderTransitionCommand closeCommand = new FinanceOrderTransitionCommand();
        closeCommand.setBookId(fixture.bookIdA());
        closeCommand.setOrderId(orderId);
        closeCommand.setAction(FinanceOrderTransitionCommand.Action.CLOSE);
        closeCommand.setRequestId(UUID.randomUUID().toString());
        closeCommand.setExpectedVersion(2L);
        requestService.transition(closeCommand);

        var audit = queryService.audit(fixture.bookIdA(), orderId, 1, 10);
        assertThat(audit.getRows()).hasSize(4);
        assertThat(audit.getTotal()).isEqualTo(4L);
        assertThat(audit.getRows()).extracting("action").containsExactly("CLOSE", "ADD", "ADD", "CREATE");
    }

    private FinanceOrderPageQuery pageQuery() {
        FinanceOrderPageQuery query = new FinanceOrderPageQuery();
        query.setBookId(fixture.bookIdA());
        return query;
    }

    private List<Long> optionOrderIds(Integer billType) {
        FinanceOrderOptionQuery query = new FinanceOrderOptionQuery();
        query.setBookId(fixture.bookIdA());
        query.setBillType(billType);
        return queryService.options(query).getRows().stream().map(FinanceOrderDetailVo::getOrderId).toList();
    }

    private com.fortuneboot.common.core.page.PageDTO<FinanceOrderEntryVo> entries(
            Long orderId, FinanceOrderEntryQuery.EntryStatus status) {
        FinanceOrderEntryQuery query = new FinanceOrderEntryQuery();
        query.setBookId(fixture.bookIdA());
        query.setOrderId(orderId);
        query.setEntryStatus(status);
        return queryService.entries(query);
    }

    private FinanceOrderCreateCommand createCommand(String title, Integer type, String dueDate, String remark) {
        FinanceOrderCreateCommand command = new FinanceOrderCreateCommand();
        command.setBookId(fixture.bookIdA());
        command.setRequestId(UUID.randomUUID().toString());
        command.setTitle(title);
        command.setType(type);
        command.setCounterpartyName("张三");
        command.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate));
        command.setRemark(remark);
        return command;
    }

    private FinanceOrderMutationVo addEntry(Long orderId, Long expectedVersion, Integer billType, String amount,
                                           Long accountId, boolean confirm, UUID requestId) {
        FinanceOrderEntryCommand entry = new FinanceOrderEntryCommand();
        entry.setBookId(fixture.bookIdA());
        entry.setOrderId(orderId);
        entry.setRequestId(requestId.toString());
        entry.setExpectedVersion(expectedVersion);
        entry.setTitle("流水");
        entry.setTradeTime(java.time.LocalDateTime.of(2026, 10, 1, 9, 0, 0));
        entry.setBillType(billType);
        entry.setOrderComponent("PRINCIPAL");
        if (billType == 7 || billType == 8) {
            entry.setAmount(null);
            entry.setCategoryAmountPair(List.of(new com.fortuneboot.domain.dto.fortune.CategoryAmountDTO(
                    1L, new java.math.BigDecimal(amount))));
        } else {
            entry.setAmount(new java.math.BigDecimal(amount));
            entry.setCategoryAmountPair(List.of());
        }
        entry.setAccountId(accountId);
        entry.setConfirm(confirm);
        entry.setInclude(false);
        return requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(orderId)
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(entry)
                .requestId(requestId.toString())
                .expectedVersion(expectedVersion)
                .fileList(List.of())
                .build());
    }

    private void login(Long userId) {
        SystemLoginUser loginUser = new SystemLoginUser(userId, false, "user" + userId, "", null);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
