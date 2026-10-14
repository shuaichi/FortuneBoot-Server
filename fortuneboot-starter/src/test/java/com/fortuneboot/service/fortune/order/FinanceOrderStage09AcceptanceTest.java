package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("阶段09补充验收：重复操作与归档并发")
@SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
class FinanceOrderStage09AcceptanceTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage09-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FinanceOrderRequestService requestService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FinanceOrderSqliteTestSupport.Fixture fixture;

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        fixture = FinanceOrderSqliteTestSupport.insertFixture(jdbcTemplate, 2);
    }

    @Test
    @DisplayName("B06：作废/恢复各重复两次，现金和待收金额只应用一次")
    void voidAndRestoreRepeatedTwice_applyEachEffectOnlyOnce() {
        mutateAdd(9, "1000.00");
        Long billId = mutateAdd(10, "400.00").getBillId();

        mutateState(FinanceOrderEntryMutation.Operation.VOID, billId);
        long voidedVersion = orderVersion();
        mutateState(FinanceOrderEntryMutation.Operation.VOID, billId);
        assertThat(accountBalance()).isEqualByComparingTo("9000.00");
        assertThat(pendingAmount()).isEqualByComparingTo("1000.00");
        assertThat(orderVersion()).isEqualTo(voidedVersion);

        mutateState(FinanceOrderEntryMutation.Operation.RESTORE, billId);
        long restoredVersion = orderVersion();
        mutateState(FinanceOrderEntryMutation.Operation.RESTORE, billId);
        assertThat(accountBalance()).isEqualByComparingTo("9400.00");
        assertThat(pendingAmount()).isEqualByComparingTo("600.00");
        assertThat(orderVersion()).isEqualTo(restoredVersion);
    }

    @Test
    @DisplayName("C08：归档与追加本金并发，不产生关闭后新增或半更新状态")
    void archiveAndAdditionalEntryConcurrently_leaveConsistentOrderState() throws Exception {
        mutateAdd(9, "1000.00");
        mutateAdd(10, "1000.00");
        long expectedVersion = orderVersion();

        FinanceOrderTransitionCommand close = transitionCommand(
                FinanceOrderTransitionCommand.Action.CLOSE, expectedVersion);
        FinanceOrderEntryMutation addPrincipal = addMutation(UUID.randomUUID().toString(), expectedVersion);

        List<Object> results = runConcurrently(() -> requestService.transition(close),
                () -> requestService.mutateEntry(addPrincipal));
        assertThat(results).hasSize(2);
        assertThat(results.stream().filter(Exception.class::isInstance)).hasSize(1);

        int status = orderStatus();
        int billCount = billCount();
        if (status == 1000) {
            assertThat(billCount).isEqualTo(2);
            assertThat(pendingAmount()).isEqualByComparingTo("0.00");
            assertThat(accountBalance()).isEqualByComparingTo("10000.00");
        } else {
            assertThat(status).isEqualTo(200);
            assertThat(billCount).isEqualTo(3);
            assertThat(pendingAmount()).isEqualByComparingTo("1000.00");
            assertThat(accountBalance()).isEqualByComparingTo("9000.00");
        }
    }

    @Test
    @DisplayName("B07：作废并恢复WRITE_OFF，现金不变且待报销先恢复后归零")
    void voidAndRestoreWriteOff_keepCashUnchangedAndRebuildPending() {
        useOrderType(1);
        mutateAdd(7, "1000.00");
        mutateAdd(8, "800.00");

        FinanceOrderEntryCommand writeOff = entryCommand(1, "200.00", orderVersion());
        writeOff.setOrderComponent(FinanceOrderComponentEnum.WRITE_OFF.getValue());
        writeOff.setAccountId(null);
        writeOff.setRemark("单位不再报销差额");
        Long writeOffId = requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(writeOff)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(orderVersion())
                .build()).getBillId();
        assertThat(accountBalance()).isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");

        mutateState(FinanceOrderEntryMutation.Operation.VOID, writeOffId);
        assertThat(accountBalance()).isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("200.00");

        mutateState(FinanceOrderEntryMutation.Operation.RESTORE, writeOffId);
        assertThat(accountBalance()).isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");
    }

    private FinanceOrderMutationVo mutateAdd(int billType, String amount) {
        long expectedVersion = orderVersion();
        return requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(entryCommand(billType, amount, expectedVersion))
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(expectedVersion)
                .build());
    }

    private void useOrderType(int type) {
        jdbcTemplate.update("update fortune_finance_order set type = ? where order_id = ?", type, fixture.orderId());
    }

    private FinanceOrderEntryMutation addMutation(String requestId, long expectedVersion) {
        return FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(entryCommand(9, "1000.00", expectedVersion))
                .requestId(requestId)
                .expectedVersion(expectedVersion)
                .build();
    }

    private FinanceOrderEntryCommand entryCommand(int billType, String amount, long expectedVersion) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(fixture.bookId());
        command.setOrderId(fixture.orderId());
        command.setTitle("阶段09流水");
        command.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        command.setAccountId(fixture.accountIdA());
        command.setBillType(billType);
        command.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        command.setConfirm(true);
        command.setInclude(true);
        command.setAmount(new BigDecimal(amount));
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal(amount))));
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(expectedVersion);
        return command;
    }

    private void mutateState(FinanceOrderEntryMutation.Operation operation, Long billId) {
        requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .billId(billId)
                .operation(operation)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(orderVersion())
                .build());
    }

    private FinanceOrderTransitionCommand transitionCommand(
            FinanceOrderTransitionCommand.Action action, long expectedVersion) {
        FinanceOrderTransitionCommand command = new FinanceOrderTransitionCommand();
        command.setBookId(fixture.bookId());
        command.setOrderId(fixture.orderId());
        command.setAction(action);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(expectedVersion);
        return command;
    }

    private List<Object> runConcurrently(Runnable first, Runnable second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Object> firstFuture = executor.submit(() -> awaitAndRun(first, ready, start));
            Future<Object> secondFuture = executor.submit(() -> awaitAndRun(second, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(firstFuture.get(10, TimeUnit.SECONDS), secondFuture.get(10, TimeUnit.SECONDS));
        }
    }

    private Object awaitAndRun(Runnable operation, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            start.await(10, TimeUnit.SECONDS);
            operation.run();
            return "success";
        } catch (Exception exception) {
            return exception;
        }
    }

    private BigDecimal accountBalance() {
        return FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA());
    }

    private long orderVersion() {
        Long value = jdbcTemplate.queryForObject(
                "select version from fortune_finance_order where order_id = ?", Long.class, fixture.orderId());
        assertThat(value).isNotNull();
        return value;
    }

    private int orderStatus() {
        Integer value = jdbcTemplate.queryForObject(
                "select status from fortune_finance_order where order_id = ?", Integer.class, fixture.orderId());
        assertThat(value).isNotNull();
        return value;
    }

    private int billCount() {
        Integer value = jdbcTemplate.queryForObject(
                "select count(*) from fortune_bill where order_id = ?", Integer.class, fixture.orderId());
        assertThat(value).isNotNull();
        return value;
    }

    private BigDecimal pendingAmount() {
        BigDecimal value = jdbcTemplate.queryForObject("""
                select coalesce(sum(case
                    when order_component = 'PRINCIPAL' and bill_type in (7, 9, 12) then amount
                    when order_component = 'PRINCIPAL' and bill_type in (8, 10, 11) then -amount
                    when order_component = 'WRITE_OFF' then -amount
                    else 0 end), 0)
                from fortune_bill
                where order_id = ? and deleted = 0 and recycle_bin = 0 and confirm = 1
                    and order_ledger_version = 1
                """, BigDecimal.class, fixture.orderId());
        assertThat(value).isNotNull();
        return value;
    }
}
