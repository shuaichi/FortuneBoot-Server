package com.fortuneboot.service.fortune.order;

import com.fortuneboot.orderapi.FinanceOrderStage03TestApplication;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderAddCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.service.fortune.FortuneBillService;
import com.fortuneboot.service.fortune.FortuneFinanceOrderService;
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
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据生命周期SQLite真实事务")
@SpringBootTest(classes = FinanceOrderStage03TestApplication.class)
class FinanceOrderLifecycleTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage03-lifecycle-");
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
    private FortuneFinanceOrderService legacyOrderService;
    @Autowired
    private FortuneBillService fortuneBillService;
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
    @DisplayName("create返回orderId/version并幂等重放")
    void createReturnsOrderIdAndIdempotentReplay() {
        FinanceOrderCreateCommand command = createCommand("张三 · 周转借款", 2, "2026-10-07", "备注");
        FinanceOrderMutationVo first = requestService.create(command);
        assertThat(first.getOrderId()).isNotNull();
        assertThat(first.getVersion()).isZero();
        assertThat(first.getReplayed()).isFalse();

        FinanceOrderMutationVo replay = requestService.create(command);
        assertThat(replay.getOrderId()).isEqualTo(first.getOrderId());
        assertThat(replay.getVersion()).isZero();
        assertThat(replay.getReplayed()).isTrue();

        FinanceOrderCreateCommand conflict = createCommand("另一个标题", 2, null, "");
        conflict.setRequestId(command.getRequestId());
        assertThatThrownBy(() -> requestService.create(conflict))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_REQUEST_CONFLICT));
    }

    @Test
    @DisplayName("旧账单页可见include=false本金流水，待确认流水仍不可见")
    void billPageListsOrderPrincipalEntriesWithoutIncludeFilterButHidesPending() {
        FinanceOrderMutationVo created = requestService.create(createCommand("借给张三", 2, null, ""));
        FinanceOrderMutationVo first = addEntry(created.getOrderId(), 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(created.getOrderId(), 1L, 9, "500.00", fixture.accountIdA(), false, UUID.randomUUID());

        FortuneBillQuery pageQuery = new FortuneBillQuery();
        pageQuery.setBookId(fixture.bookIdA());
        com.fortuneboot.common.core.page.PageDTO<FortuneBillBo> page = fortuneBillService.getPage(pageQuery);

        assertThat(page.getRows()).hasSize(1);
        assertThat(page.getRows().get(0).getBillId()).isEqualTo(first.getBillId());
        assertThat(page.getRows().get(0).getBillType()).isEqualTo(9);
        assertThat(page.getRows().get(0).getInclude()).isFalse();
    }

    @Test
    @DisplayName("create由服务端填充币种、READY、金额0和counterparty快照")
    void createFillsServerManagedFieldsAndCounterpartySnapshot() {
        FinanceOrderCreateCommand command = createCommand("借给张三", 2, null, "");
        command.setCounterpartyId(fixture.payeeId());
        FinanceOrderMutationVo created = requestService.create(command);

        var detail = queryService.detail(fixture.bookIdA(), created.getOrderId());
        assertThat(detail.getStatus()).isEqualTo(100);
        assertThat(detail.getStatusDesc()).isEqualTo("草稿");
        assertThat(detail.getTypeDesc()).isEqualTo("债权单");
        assertThat(detail.getCurrencyCode()).isEqualTo("CNY");
        assertThat(detail.getReconciliationStatus()).isEqualTo("READY");
        assertThat(detail.getCounterpartyId()).isEqualTo(fixture.payeeId());
        assertThat(detail.getCounterpartyName()).isEqualTo("张三");
        assertThat(detail.getOutAmount()).isEqualTo("0.00");
        assertThat(detail.getInAmount()).isEqualTo("0.00");
        assertThat(detail.getAdjustedAmount()).isEqualTo("0.00");
        assertThat(detail.getPendingAmount()).isEqualTo("0.00");
        assertThat(detail.getSettlementState()).isEqualTo("EMPTY");
    }

    @Test
    @DisplayName("旧add保留缺省对象名“未填写”，非法type拒绝")
    void legacyAddDefaultsCounterpartyNameAndRejectsInvalidType() {
        FortuneFinanceOrderAddCommand command = new FortuneFinanceOrderAddCommand();
        command.setBookId(fixture.bookIdA());
        command.setTitle("旧接口单据");
        command.setType(2);
        command.setRemark("备注");
        legacyOrderService.add(command);

        Long orderId = jdbcTemplate.queryForObject("select order_id from fortune_finance_order limit 1", Long.class);
        var detail = queryService.detail(fixture.bookIdA(), orderId);
        assertThat(detail.getCounterpartyName()).isEqualTo("未填写");
        assertThat(detail.getCounterpartyId()).isNull();

        FortuneFinanceOrderAddCommand invalid = new FortuneFinanceOrderAddCommand();
        invalid.setBookId(fixture.bookIdA());
        invalid.setTitle("非法类型");
        invalid.setType(99);
        assertThatThrownBy(() -> legacyOrderService.add(invalid))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_TYPE_MISMATCH));
    }

    @Test
    @DisplayName("modify逐字段更新并支持清空备注和日期，type和版本冲突拒绝")
    void modifyUpdatesWhitelistedFieldsAndDetectsConflicts() {
        FinanceOrderMutationVo created = requestService.create(createCommand("原标题", 1, "2026-10-07", "备注"));

        FinanceOrderMetadataCommand modify = modifyCommand(created.getOrderId(), 1, 0L);
        modify.setTitle("新标题");
        modify.setRemark("");
        modify.setDueDate(null);
        FinanceOrderMutationVo modified = requestService.modify(modify);
        assertThat(modified.getVersion()).isEqualTo(1L);

        var detail = queryService.detail(fixture.bookIdA(), created.getOrderId());
        assertThat(detail.getTitle()).isEqualTo("新标题");
        assertThat(detail.getRemark()).isEmpty();
        assertThat(detail.getDueDate()).isNull();
        assertThat(detail.getVersion()).isEqualTo(1L);

        FinanceOrderMetadataCommand typeChanged = modifyCommand(created.getOrderId(), 2, 1L);
        typeChanged.setTitle("类型变化");
        assertThatThrownBy(() -> requestService.modify(typeChanged))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_TYPE_MISMATCH));

        FinanceOrderMetadataCommand stale = modifyCommand(created.getOrderId(), 1, 0L);
        stale.setTitle("过期版本");
        assertThatThrownBy(() -> requestService.modify(stale))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_VERSION_CONFLICT));
    }

    @Test
    @DisplayName("counterparty只在无流水历史时可改，有流水后锁定")
    void counterpartyChangeRequiresNoBillHistory() {
        FinanceOrderMutationVo created = requestService.create(createCommand("无流水单据", 2, null, ""));

        FinanceOrderMetadataCommand change = modifyCommand(created.getOrderId(), 2, 0L);
        change.setCounterpartyId(fixture.payeeId());
        change.setCounterpartyName("王五");
        FinanceOrderMutationVo changed = requestService.modify(change);
        assertThat(changed.getVersion()).isEqualTo(1L);

        FinanceOrderMutationVo withBill = requestService.create(createCommand("有流水单据", 2, null, ""));
        addEntry(withBill.getOrderId(), 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());

        FinanceOrderMetadataCommand locked = modifyCommand(withBill.getOrderId(), 2, 1L);
        locked.setCounterpartyName("李四");
        assertThatThrownBy(() -> requestService.modify(locked))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_HISTORY_EXISTS));

        FinanceOrderMetadataCommand stillEditable = modifyCommand(withBill.getOrderId(), 2, 1L);
        stillEditable.setTitle("仍可改标题");
        stillEditable.setDueDate(LocalDate.of(2026, 10, 7));
        stillEditable.setRemark("仍可改备注");
        assertThat(requestService.modify(stillEditable).getVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("close重新按流水聚合校验：未结清、待确认、归档、重开")
    void closeRecomputesLedgerAndTransitionLifecycle() {
        FinanceOrderMutationVo created = requestService.create(createCommand("生命周期单据", 2, "2026-10-07", ""));
        addEntry(created.getOrderId(), 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        long issuedVersion = 1L;

        assertThatThrownBy(() -> transition(created.getOrderId(), issuedVersion,
                FinanceOrderTransitionCommand.Action.CLOSE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_NOT_SETTLED));

        FinanceOrderMutationVo recover = addEntry(created.getOrderId(), issuedVersion, 10, "400.00",
                fixture.accountIdA(), true, UUID.randomUUID());
        long recoveredVersion = recover.getVersion();

        // 破坏缓存金额，验证close不信任缓存而重新聚合
        jdbcTemplate.update("update fortune_finance_order set in_amount = 1000 where order_id = ?",
                created.getOrderId());
        assertThatThrownBy(() -> transition(created.getOrderId(), recoveredVersion,
                FinanceOrderTransitionCommand.Action.CLOSE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_NOT_SETTLED));

        FinanceOrderMutationVo unconfirmed = addEntry(created.getOrderId(), recoveredVersion, 10, "600.00",
                fixture.accountIdA(), false, UUID.randomUUID());
        long unconfirmedVersion = unconfirmed.getVersion();
        assertThatThrownBy(() -> transition(created.getOrderId(), unconfirmedVersion,
                FinanceOrderTransitionCommand.Action.CLOSE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_HAS_PENDING_ENTRIES));

        FinanceOrderMutationVo confirmed = requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(created.getOrderId())
                .billId(unconfirmed.getBillId())
                .operation(FinanceOrderEntryMutation.Operation.CONFIRM)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(unconfirmedVersion)
                .build());

        FinanceOrderMutationVo closed = transition(created.getOrderId(), confirmed.getVersion(),
                FinanceOrderTransitionCommand.Action.CLOSE);
        assertThat(closed.getVersion()).isEqualTo(confirmed.getVersion() + 1);
        var archived = queryService.detail(fixture.bookIdA(), created.getOrderId());
        assertThat(archived.getStatus()).isEqualTo(1000);
        assertThat(archived.getCloseTime()).isNotNull();
        assertThat(archived.getPendingAmount()).isEqualTo("0.00");
        assertThat(archived.getIssuedAmount()).isEqualTo("1000.00");
        assertThat(archived.getSettledAmount()).isEqualTo("1000.00");

        FinanceOrderMutationVo reClose = transition(created.getOrderId(), closed.getVersion(),
                FinanceOrderTransitionCommand.Action.CLOSE);
        assertThat(reClose.getReplayed()).isFalse();
        assertThat(reClose.getVersion()).isEqualTo(closed.getVersion());

        FinanceOrderMutationVo reopened = transition(created.getOrderId(), closed.getVersion(),
                FinanceOrderTransitionCommand.Action.REOPEN);
        assertThat(reopened.getVersion()).isEqualTo(closed.getVersion() + 1);
        var reopenedDetail = queryService.detail(fixture.bookIdA(), created.getOrderId());
        assertThat(reopenedDetail.getStatus()).isEqualTo(200);
        assertThat(reopenedDetail.getCloseTime()).isNull();
        assertThat(reopenedDetail.getPendingAmount()).isEqualTo("0.00");

        FinanceOrderMutationVo reReopen = transition(created.getOrderId(), reopened.getVersion(),
                FinanceOrderTransitionCommand.Action.REOPEN);
        assertThat(reReopen.getVersion()).isEqualTo(reopened.getVersion());
    }

    @Test
    @DisplayName("空单删除包含已作废和legacy deleted流水检查")
    void removeOnlyAllowsEmptyOrderIncludingVoidedAndDeletedHistory() {
        FinanceOrderMutationVo empty = requestService.create(createCommand("空单据", 1, null, ""));
        FinanceOrderMutationVo removed = transition(empty.getOrderId(), 0L, FinanceOrderTransitionCommand.Action.REMOVE);
        assertThat(removed.getOrderId()).isEqualTo(empty.getOrderId());
        Integer deletedCount = jdbcTemplate.queryForObject(
                "select count(*) from fortune_finance_order where order_id = ? and deleted = 1",
                Integer.class, empty.getOrderId());
        assertThat(deletedCount).isEqualTo(1);

        FinanceOrderMutationVo withActiveBill = requestService.create(createCommand("有效流水单据", 2, null, ""));
        addEntry(withActiveBill.getOrderId(), 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        assertThatThrownBy(() -> transition(withActiveBill.getOrderId(), 1L, FinanceOrderTransitionCommand.Action.REMOVE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_HISTORY_EXISTS));

        FinanceOrderMutationVo withVoidedBill = requestService.create(createCommand("作废流水单据", 2, null, ""));
        FinanceOrderMutationVo added = addEntry(withVoidedBill.getOrderId(), 0L, 9, "1000.00",
                fixture.accountIdA(), true, UUID.randomUUID());
        requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(withVoidedBill.getOrderId())
                .billId(added.getBillId())
                .operation(FinanceOrderEntryMutation.Operation.VOID)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(added.getVersion())
                .build());
        assertThatThrownBy(() -> transition(withVoidedBill.getOrderId(), added.getVersion(),
                FinanceOrderTransitionCommand.Action.REMOVE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_HISTORY_EXISTS));

        FinanceOrderMutationVo legacyDeleted = requestService.create(createCommand("历史删除流水单据", 2, null, ""));
        jdbcTemplate.update("""
                insert into fortune_bill(book_id, title, trade_time, account_id, amount, converted_amount, bill_type,
                    confirm, include, order_id, order_component, order_ledger_version, recycle_bin, deleted)
                values (?, '历史流水', '2026-10-01 09:00:00', ?, 100.00, 100.00, 9, 1, 1, ?, 'PRINCIPAL', 1, 0, 1)
                """, fixture.bookIdA(), fixture.accountIdA(), legacyDeleted.getOrderId());
        assertThatThrownBy(() -> transition(legacyDeleted.getOrderId(), 0L, FinanceOrderTransitionCommand.Action.REMOVE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_HISTORY_EXISTS));
    }

    @Test
    @DisplayName("旧using接口保持100到200；旧close/reopen委托公共服务")
    void legacyLifecycleMethodsDelegateToCommonService() {
        FortuneFinanceOrderAddCommand command = new FortuneFinanceOrderAddCommand();
        command.setBookId(fixture.bookIdA());
        command.setTitle("旧生命周期");
        command.setType(2);
        legacyOrderService.add(command);
        Long orderId = jdbcTemplate.queryForObject("select order_id from fortune_finance_order limit 1", Long.class);

        legacyOrderService.using(fixture.bookIdA(), orderId);
        assertThat(queryService.detail(fixture.bookIdA(), orderId).getStatus()).isEqualTo(200);
        assertThatThrownBy(() -> legacyOrderService.using(fixture.bookIdA(), orderId))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_USING_STATUS_ERROR));

        addEntry(orderId, queryService.detail(fixture.bookIdA(), orderId).getVersion(), 9, "1000.00",
                fixture.accountIdA(), true, UUID.randomUUID());
        addEntry(orderId, 2L, 10, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());

        legacyOrderService.close(fixture.bookIdA(), orderId);
        var closed = queryService.detail(fixture.bookIdA(), orderId);
        assertThat(closed.getStatus()).isEqualTo(1000);
        assertThat(closed.getCloseTime()).isNotNull();

        legacyOrderService.reopen(fixture.bookIdA(), orderId);
        var reopened = queryService.detail(fixture.bookIdA(), orderId);
        assertThat(reopened.getStatus()).isEqualTo(200);
        assertThat(reopened.getCloseTime()).isNull();
    }

    @Test
    @DisplayName("待核对单据只读：modify/close/remove均拒绝")
    void reviewRequiredOrderIsReadOnly() {
        FinanceOrderMutationVo created = requestService.create(createCommand("待核对单据", 2, null, ""));
        jdbcTemplate.update("update fortune_finance_order set reconciliation_status = 'REVIEW_REQUIRED' where order_id = ?",
                created.getOrderId());

        FinanceOrderMetadataCommand modify = modifyCommand(created.getOrderId(), 2, 0L);
        modify.setTitle("不可修改");
        assertThatThrownBy(() -> requestService.modify(modify))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED));
        assertThatThrownBy(() -> transition(created.getOrderId(), 0L, FinanceOrderTransitionCommand.Action.CLOSE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED));
        assertThatThrownBy(() -> transition(created.getOrderId(), 0L, FinanceOrderTransitionCommand.Action.REMOVE))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED));
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

    private FinanceOrderMetadataCommand modifyCommand(Long orderId, Integer type, Long expectedVersion) {
        FinanceOrderMetadataCommand command = new FinanceOrderMetadataCommand();
        command.setBookId(fixture.bookIdA());
        command.setRequestId(UUID.randomUUID().toString());
        command.setOrderId(orderId);
        command.setExpectedVersion(expectedVersion);
        command.setTitle("修改标题");
        command.setType(type);
        command.setCounterpartyId(null);
        command.setCounterpartyName("张三");
        command.setRemark("修改备注");
        command.setDueDate(null);
        return command;
    }

    private FinanceOrderMutationVo transition(Long orderId, Long expectedVersion,
                                              FinanceOrderTransitionCommand.Action action) {
        FinanceOrderTransitionCommand command = new FinanceOrderTransitionCommand();
        command.setBookId(fixture.bookIdA());
        command.setOrderId(orderId);
        command.setAction(action);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(expectedVersion);
        return requestService.transition(command);
    }

    private FinanceOrderMutationVo addEntry(Long orderId, Long expectedVersion, Integer billType, String amount,
                                            Long accountId, boolean confirm, UUID requestId) {
        FinanceOrderEntryCommand entry = new FinanceOrderEntryCommand();
        entry.setBookId(fixture.bookIdA());
        entry.setOrderId(orderId);
        entry.setRequestId(requestId.toString());
        entry.setExpectedVersion(expectedVersion);
        entry.setTitle("借给张三");
        entry.setTradeTime(java.time.LocalDateTime.of(2026, 10, 1, 9, 0, 0));
        entry.setBillType(billType);
        entry.setOrderComponent("PRINCIPAL");
        entry.setAmount(new java.math.BigDecimal(amount));
        entry.setCategoryAmountPair(List.of());
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
