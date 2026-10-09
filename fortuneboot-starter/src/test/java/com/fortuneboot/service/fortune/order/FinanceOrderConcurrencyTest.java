package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.EnabledIf;
import org.flywaydb.core.Flyway;
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

@DisplayName("单据写入SQLite与MySQL并发")
@SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
class FinanceOrderConcurrencyTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage02-concurrent-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FinanceOrderRequestService requestService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FortuneBillRepo fortuneBillRepo;

    @Autowired
    private FinanceOrderLedgerCalculator ledgerCalculator;

    private FinanceOrderSqliteTestSupport.Fixture fixture;

    @Nested
    @DisplayName("MySQL测试schema两连接并发")
    @EnabledIf(value = "mysqlConfigured", disabledReason = "未配置名称包含test的FINANCE_ORDER_TEST_MYSQL_*专用测试库")
    @SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
    class MysqlTwoConnectionConcurrencyTest {

        private static final String MYSQL_URL = System.getenv("FINANCE_ORDER_TEST_MYSQL_URL");
        private static final String MYSQL_USER = System.getenv("FINANCE_ORDER_TEST_MYSQL_USER");
        private static final String MYSQL_PASSWORD = System.getenv("FINANCE_ORDER_TEST_MYSQL_PASSWORD");

        private FinanceOrderSqliteTestSupport.Fixture mysqlFixture;

        @Autowired
        private FinanceOrderRequestService mysqlRequestService;

        @Autowired
        private JdbcTemplate mysqlJdbcTemplate;

        static boolean mysqlConfigured() {
            return MYSQL_URL != null && MYSQL_USER != null && MYSQL_PASSWORD != null
                    && MYSQL_URL.toLowerCase().contains("test");
        }

        @DynamicPropertySource
        static void mysqlProperties(DynamicPropertyRegistry registry) {
            if (!mysqlConfigured()) {
                throw new IllegalStateException("MySQL专用测试库环境不完整");
            }
            Flyway flyway = Flyway.configure()
                    .dataSource(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD)
                    .locations("classpath:db/migration/mysql")
                    .cleanDisabled(false)
                    .load();
            flyway.clean();
            flyway.migrate();
            registry.add("finance-order.test.db-type", () -> "mysql");
            registry.add("finance-order.test.mysql-url", () -> MYSQL_URL);
            registry.add("finance-order.test.mysql-user", () -> MYSQL_USER);
            registry.add("finance-order.test.mysql-password", () -> MYSQL_PASSWORD);
            registry.add("db.type", () -> "mysql");
        }

        @BeforeEach
        void setUpMysql() {
            FinanceOrderSqliteTestSupport.cleanBusinessTables(mysqlJdbcTemplate);
            mysqlFixture = FinanceOrderSqliteTestSupport.insertFixture(mysqlJdbcTemplate, 2);
            mysqlRequestService.mutateEntry(addMutation(UUID.randomUUID().toString(), "600.00", 9));
        }

        @Test
        @DisplayName("两个独立事务并发收款400：一成功一冲突，最终待收200")
        void concurrentDifferentRequests_onlyOneSucceeds() throws Exception {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch start = new CountDownLatch(1);
            try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
                Future<?> first = executor.submit(() -> awaitAndMutate(
                        addMutation(UUID.randomUUID().toString(), "400.00", 10), ready, start));
                Future<?> second = executor.submit(() -> awaitAndMutate(
                        addMutation(UUID.randomUUID().toString(), "400.00", 10), ready, start));
                assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
                start.countDown();
                first.get(10, TimeUnit.SECONDS);
                second.get(10, TimeUnit.SECONDS);
            }

            assertThat(mysqlJdbcTemplate.queryForObject("select count(*) from fortune_bill", Integer.class))
                    .isEqualTo(2);
            assertThat(FinanceOrderSqliteTestSupport.accountBalance(mysqlJdbcTemplate, mysqlFixture.accountIdA()))
                    .isEqualByComparingTo("9800.00");
            assertThat(pendingAmount()).isEqualByComparingTo("200.00");
        }

        private Object awaitAndMutate(FinanceOrderEntryMutation mutation, CountDownLatch ready, CountDownLatch start) {
            ready.countDown();
            try {
                start.await(10, TimeUnit.SECONDS);
                return mysqlRequestService.mutateEntry(mutation);
            } catch (Exception exception) {
                return exception;
            }
        }

        private FinanceOrderEntryMutation addMutation(String requestId, String amount, int billType) {
            FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
            command.setBookId(mysqlFixture.bookId());
            command.setOrderId(mysqlFixture.orderId());
            command.setTitle("MySQL并发流水");
            command.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
            command.setAccountId(mysqlFixture.accountIdA());
            command.setBillType(billType);
            command.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
            command.setConfirm(true);
            command.setInclude(true);
            command.setAmount(new BigDecimal(amount));
            command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal(amount))));
            command.setRequestId(requestId);
            command.setExpectedVersion(currentVersion());

            return FinanceOrderEntryMutation.builder()
                    .bookId(mysqlFixture.bookId())
                    .orderId(mysqlFixture.orderId())
                    .operation(FinanceOrderEntryMutation.Operation.ADD)
                    .entryCommand(command)
                    .requestId(requestId)
                    .expectedVersion(currentVersion())
                    .build();
        }

        private long currentVersion() {
            Long version = mysqlJdbcTemplate.queryForObject(
                    "select version from fortune_finance_order where order_id = ?", Long.class, mysqlFixture.orderId());
            assertThat(version).isNotNull();
            return version;
        }

        private BigDecimal pendingAmount() {
            List<FinanceOrderLedgerEntry> entries = mysqlJdbcTemplate.query(
                    "select bill_id,bill_type,order_component,amount,trade_time,confirm,deleted,recycle_bin,order_ledger_version from fortune_bill where order_id = ?",
                    (rs, rowNum) -> new FinanceOrderLedgerEntry(
                            rs.getLong("bill_id"), rs.getInt("bill_type"), rs.getString("order_component"),
                            rs.getBigDecimal("amount"), rs.getTimestamp("trade_time").toLocalDateTime(),
                            rs.getBoolean("confirm"), rs.getBoolean("deleted"), rs.getBoolean("recycle_bin"),
                            rs.getInt("order_ledger_version")), mysqlFixture.orderId());
            return new FinanceOrderLedgerCalculator().calculate(2, entries).pendingAmount();
        }
    }

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        fixture = FinanceOrderSqliteTestSupport.insertFixture(jdbcTemplate, 2);
        requestService.mutateEntry(addMutation(UUID.randomUUID().toString(), "600.00", 9));
    }

    @Test
    @DisplayName("剩余600时两个不同requestId并发收款400：一成功一冲突，最终待收200")
    void concurrentDifferentRequests_onlyOneSettlementSucceeds() throws Exception {
        List<Object> results = runConcurrentMutations(
                addMutation(UUID.randomUUID().toString(), "400.00", 10),
                addMutation(UUID.randomUUID().toString(), "400.00", 10));

        assertThat(billCount()).isEqualTo(2);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("同requestId并发最终只入一笔，成功回执的billId一致")
    void concurrentSameRequest_onlyOneCashEffectAndSameBillId() throws Exception {
        String requestId = UUID.randomUUID().toString();
        List<Object> results = runConcurrentMutations(
                addMutation(requestId, "400.00", 10),
                addMutation(requestId, "400.00", 10));

        assertThat(billCount()).isEqualTo(2);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("200.00");
        List<Long> successBillIds = results.stream()
                .filter(FinanceOrderMutationVo.class::isInstance)
                .map(FinanceOrderMutationVo.class::cast)
                .map(FinanceOrderMutationVo::getBillId)
                .toList();
        assertThat(successBillIds).isNotEmpty();
        assertThat(successBillIds).allSatisfy(billId -> assertThat(billId).isEqualTo(successBillIds.get(0)));
        assertThat(requestReceipts()).isEqualTo(2);
    }

    private List<Object> runConcurrentMutations(FinanceOrderEntryMutation first, FinanceOrderEntryMutation second)
            throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<?> firstFuture = executor.submit(() -> awaitAndMutate(first, ready, start));
            Future<?> secondFuture = executor.submit(() -> awaitAndMutate(second, ready, start));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            Object firstResult = firstFuture.get(10, TimeUnit.SECONDS);
            Object secondResult = secondFuture.get(10, TimeUnit.SECONDS);
            return List.of(firstResult, secondResult);
        }
    }

    private Object awaitAndMutate(FinanceOrderEntryMutation mutation, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            start.await(10, TimeUnit.SECONDS);
            return requestService.mutateEntry(mutation);
        } catch (Exception exception) {
            return exception;
        }
    }

    private FinanceOrderEntryMutation addMutation(String requestId, String amount, int billType) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(fixture.bookId());
        command.setOrderId(fixture.orderId());
        command.setTitle("并发流水");
        command.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        command.setAccountId(fixture.accountIdA());
        command.setBillType(billType);
        command.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        command.setConfirm(true);
        command.setInclude(true);
        command.setAmount(new BigDecimal(amount));
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal(amount))));
        command.setRequestId(requestId);
        command.setExpectedVersion(currentVersion());

        return FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(command)
                .requestId(requestId)
                .expectedVersion(currentVersion())
                .build();
    }

    private long currentVersion() {
        Long version = jdbcTemplate.queryForObject(
                "select version from fortune_finance_order where order_id = ?", Long.class, fixture.orderId());
        assertThat(version).isNotNull();
        return version;
    }

    private BigDecimal pendingAmount() {
        return ledgerSnapshot().pendingAmount();
    }

    private FinanceOrderLedgerSnapshot ledgerSnapshot() {
        return ledgerCalculator.calculate(2, fortuneBillRepo.getByOrderId(fixture.orderId()).stream()
                .map(this::ledgerEntry)
                .toList());
    }

    private FinanceOrderLedgerEntry ledgerEntry(FortuneBillEntity bill) {
        return new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(), bill.getOrderComponent(),
                bill.getAmount(), bill.getTradeTime(), Boolean.TRUE.equals(bill.getConfirm()),
                Boolean.TRUE.equals(bill.getDeleted()), Boolean.TRUE.equals(bill.getRecycleBin()),
                bill.getOrderLedgerVersion() == null ? 0 : bill.getOrderLedgerVersion());
    }

    private int billCount() {
        return jdbcTemplate.queryForObject("select count(*) from fortune_bill", Integer.class);
    }

    private int requestReceipts() {
        return jdbcTemplate.queryForObject("select count(*) from fortune_finance_order_request", Integer.class);
    }
}
