package com.fortuneboot.service.fortune.order;

import com.fortuneboot.orderapi.FinanceOrderStage03TestApplication;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.service.permission.FortunePermissionService;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据权限与归属边界")
@SpringBootTest(classes = FinanceOrderStage03TestApplication.class)
class FinanceOrderPermissionTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage03-permission-");
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
    private FortunePermissionService permissionService;
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
    @DisplayName("actor按状态获得can*和allowedEntryActions")
    void actorGetsCapabilitiesByStatus() {
        Long draft = requestService.create(createCommand("草稿债权", 2, null, "")).getOrderId();
        var draftDetail = queryService.detail(fixture.bookIdA(), draft);
        assertThat(draftDetail.isCanEdit()).isTrue();
        assertThat(draftDetail.isCanDelete()).isTrue();
        assertThat(draftDetail.isCanClose()).isFalse();
        assertThat(draftDetail.isCanReopen()).isFalse();
        assertThat(draftDetail.getAllowedEntryActions()).containsOnly("LOAN_OUT");

        Long using = requestService.create(createCommand("进行中债权", 2, null, "")).getOrderId();
        FinanceOrderMutationVo issued = addEntry(using, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        var usingDetail = queryService.detail(fixture.bookIdA(), using);
        assertThat(usingDetail.isCanEdit()).isTrue();
        assertThat(usingDetail.isCanDelete()).isFalse();
        assertThat(usingDetail.isCanClose()).isFalse();
        assertThat(usingDetail.isCanReopen()).isFalse();
        assertThat(usingDetail.getAllowedEntryActions())
                .containsOnly("LOAN_OUT", "LOAN_RECOVER", "INTEREST_IN", "FEE", "WRITE_OFF");

        addEntry(using, issued.getVersion(), 10, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo closed = transition(using, 2L,
                com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand.Action.CLOSE);
        var archivedDetail = queryService.detail(fixture.bookIdA(), using);
        assertThat(archivedDetail.isCanEdit()).isFalse();
        assertThat(archivedDetail.isCanClose()).isFalse();
        assertThat(archivedDetail.isCanReopen()).isTrue();
        assertThat(archivedDetail.getAllowedEntryActions()).isEmpty();
    }

    @Test
    @DisplayName("访客只读：detail/page所有can*=false且actions为空，可读不可写")
    void visitorSeesReadOnlyCapabilities() {
        Long orderId = requestService.create(createCommand("访客只读单据", 2, null, "")).getOrderId();
        addEntry(orderId, 0L, 9, "1000.00", fixture.accountIdA(), true, UUID.randomUUID());

        login(fixture.visitorUserId());
        assertThat(permissionService.bookActorPermission(fixture.bookIdA())).isFalse();
        assertThat(permissionService.bookVisitorPermission(fixture.bookIdA())).isTrue();

        var detail = queryService.detail(fixture.bookIdA(), orderId);
        assertThat(detail.isCanEdit()).isFalse();
        assertThat(detail.isCanDelete()).isFalse();
        assertThat(detail.isCanClose()).isFalse();
        assertThat(detail.isCanReopen()).isFalse();
        assertThat(detail.getAllowedEntryActions()).isEmpty();
        assertThat(detail.getPendingAmount()).isEqualTo("1000.00");

        FinanceOrderPageQuery query = new FinanceOrderPageQuery();
        query.setBookId(fixture.bookIdA());
        var page = queryService.page(query);
        assertThat(page.getRows()).isNotEmpty();
        assertThat(page.getRows()).allSatisfy(row -> {
            assertThat(row.isCanEdit()).isFalse();
            assertThat(row.isCanDelete()).isFalse();
            assertThat(row.getAllowedEntryActions()).isEmpty();
        });

        FinanceOrderEntryQuery entryQuery = new FinanceOrderEntryQuery();
        entryQuery.setBookId(fixture.bookIdA());
        entryQuery.setOrderId(orderId);
        assertThat(queryService.entries(entryQuery).getRows().getFirst().getAllowedActions()).isEmpty();
    }

    @Test
    @DisplayName("可访问账本A但目标单据属于B/C时全部拒绝")
    void foreignOrderIsRejectedEvenWhenPathBookAccessible() {
        Long foreignOrderId = requestService.create(createCommand("账本B单据", 2, null, "")).getOrderId();
        FinanceOrderMutationVo foreignBill = addEntry(foreignOrderId, 0L, 9, "1000.00",
                fixture.accountIdA(), true, UUID.randomUUID());
        jdbcTemplate.update("update fortune_finance_order set book_id = ? where order_id = ?",
                fixture.bookIdB(), foreignOrderId);

        assertThatThrownBy(() -> queryService.detail(fixture.bookIdA(), foreignOrderId))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_BOOK_NOT_MATCH));
        assertThatThrownBy(() -> queryService.entries(entryQuery(fixture.bookIdA(), foreignOrderId)))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_BOOK_NOT_MATCH));
        assertThatThrownBy(() -> queryService.audit(fixture.bookIdA(), foreignOrderId, 1, 10))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_BOOK_NOT_MATCH));
        assertThatThrownBy(() -> requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                        .bookId(fixture.bookIdA())
                        .orderId(foreignOrderId)
                        .billId(foreignBill.getBillId())
                        .operation(FinanceOrderEntryMutation.Operation.RESTORE)
                        .requestId(UUID.randomUUID().toString())
                        .expectedVersion(foreignBill.getVersion())
                        .build()))
                .isInstanceOfSatisfying(ApiException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.Business.ORDER_BOOK_NOT_MATCH));
    }

    @Test
    @DisplayName("无任何分组关系的用户只有访客行为且不可访问他人账本")
    void outsiderHasNoAccessToForeignGroupBooks() {
        Long orderId = requestService.create(createCommand("分组内单据", 2, null, "")).getOrderId();

        login(fixture.outsiderUserId());
        assertThat(permissionService.bookVisitorPermission(fixture.bookIdA())).isFalse();
        assertThat(permissionService.bookActorPermission(fixture.bookIdA())).isFalse();
    }

    private FinanceOrderEntryQuery entryQuery(Long bookId, Long orderId) {
        FinanceOrderEntryQuery query = new FinanceOrderEntryQuery();
        query.setBookId(bookId);
        query.setOrderId(orderId);
        return query;
    }

    private FinanceOrderMutationVo transition(Long orderId, Long expectedVersion,
            com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand.Action action) {
        var command = new com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand();
        command.setBookId(fixture.bookIdA());
        command.setOrderId(orderId);
        command.setAction(action);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(expectedVersion);
        return requestService.transition(command);
    }

    private FinanceOrderCreateCommand createCommand(String title, Integer type, String dueDate, String remark) {
        FinanceOrderCreateCommand command = new FinanceOrderCreateCommand();
        command.setBookId(fixture.bookIdA());
        command.setRequestId(UUID.randomUUID().toString());
        command.setTitle(title);
        command.setType(type);
        command.setCounterpartyName("张三");
        command.setDueDate(dueDate == null ? null : java.time.LocalDate.parse(dueDate));
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
