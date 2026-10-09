package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.domain.command.fortune.FortuneBookModifyCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.orderapi.FinanceOrderStage04TestApplication;
import com.fortuneboot.service.fortune.FortuneAccountService;
import com.fortuneboot.service.fortune.FortuneBookService;
import com.fortuneboot.service.fortune.FortuneGroupService;
import com.fortuneboot.service.fortune.FortunePayeeService;
import com.fortuneboot.service.fortune.FortuneBalanceSnapshotService;
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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("相邻删除、账本币种与快照并发保护")
@SpringBootTest(classes = FinanceOrderStage04TestApplication.class)
class FinanceOrderReferenceGuardTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage04-reference-");
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
    private FortuneAccountService accountService;
    @Autowired
    private FortunePayeeService payeeService;
    @Autowired
    private FortuneBookService bookService;
    @Autowired
    private FortuneGroupService groupService;
    @Autowired
    private FortuneBalanceSnapshotService snapshotService;
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
    @DisplayName("账户被作废历史流水引用时仍禁止物理删除")
    void accountWithVoidedOrderHistoryCannotBeDeleted() {
        Long orderId = createOrder("账户引用债权", 2);
        FinanceOrderMutationVo resultVersion = addLoan(orderId, "100.00");
        requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(orderId)
                .billId(resultVersion.getBillId())
                .operation(FinanceOrderEntryMutation.Operation.VOID)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(resultVersion.getVersion())
                .build());

        assertThatThrownBy(() -> accountService.remove(fixture.groupId(), fixture.accountIdA()))
                .isInstanceOf(ApiException.class);
        assertThat(objectDeleted("fortune_account", "account_id", fixture.accountIdA())).isFalse();
    }

    @Test
    @DisplayName("交易对象被单据或作废流水引用时禁止物理删除")
    void payeeWithOrderOrVoidedBillReferenceCannotBeDeleted() {
        Long orderId = createOrder("对象引用债权", 2);
        FinanceOrderMutationVo loan = addLoan(orderId, "100.00");
        jdbcTemplate.update("update fortune_bill set payee_id = ? where bill_id = ?",
                fixture.payeeId(), loan.getBillId());

        assertThatThrownBy(() -> payeeService.remove(fixture.bookIdA(), fixture.payeeId()))
                .isInstanceOf(ApiException.class);
        assertThat(objectDeleted("fortune_payee", "payee_id", fixture.payeeId())).isFalse();
    }

    @Test
    @DisplayName("有单据历史的账本禁止改默认币种，账本与分组删除先做完整引用检查")
    void bookCurrencyAndBatchDeletionAreGuarded() {
        createOrder("账本引用单据", 1);

        FortuneBookModifyCommand command = new FortuneBookModifyCommand();
        command.setGroupId(fixture.groupId());
        command.setBookId(fixture.bookIdA());
        command.setBookName("账本A");
        command.setDefaultCurrency("USD");
        command.setEnable(true);
        assertThatThrownBy(() -> bookService.modify(command)).isInstanceOf(ApiException.class);
        assertThat(jdbcTemplate.queryForObject(
                "select default_currency from fortune_book where book_id = ?", String.class, fixture.bookIdA()))
                .isEqualTo("CNY");

        assertThatThrownBy(() -> bookService.remove(fixture.bookIdA())).isInstanceOf(ApiException.class);
        assertThat(objectDeleted("fortune_book", "book_id", fixture.bookIdA())).isFalse();
        assertThat(objectDeleted("fortune_category", "category_id", 1L)).isFalse();

        assertThatThrownBy(() -> groupService.remove(fixture.groupId())).isInstanceOf(ApiException.class);
        assertThat(objectDeleted("fortune_group", "group_id", fixture.groupId())).isFalse();
    }

    @Test
    @DisplayName("基础对象删除与新增关联流水共用组锁，结果不留下悬挂引用")
    void concurrentDeleteAndAddNeverLeaveDanglingReference() throws Exception {
        Long orderId = createOrder("并发对象", 2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> removeFuture = executor.submit(() -> {
                await(start);
                try {
                    accountService.remove(fixture.groupId(), fixture.accountIdA());
                    return null;
                } catch (Exception exception) {
                    return exception;
                }
            });
            Future<?> addFuture = executor.submit(() -> {
                await(start);
                try {
                    return addLoan(orderId, "100.00");
                } catch (Exception exception) {
                    return exception;
                }
            });
            start.countDown();
            Object removeResult = removeFuture.get(10, TimeUnit.SECONDS);
            Object addResult = addFuture.get(10, TimeUnit.SECONDS);

            boolean accountDeleted = objectDeleted("fortune_account", "account_id", fixture.accountIdA());
            boolean billExists = jdbcTemplate.queryForObject(
                    "select count(*) from fortune_bill where order_id = ?", Integer.class, orderId) > 0;
            assertThat(accountDeleted && billExists).isFalse();
            if (billExists) {
                assertThat(addResult).isNotInstanceOf(Exception.class);
                assertThat(accountDeleted).isFalse();
            } else {
                assertThat(addResult).isInstanceOf(Exception.class);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("全量快照重建与单据写入串行化，最终快照与真实余额一致")
    void fullSnapshotAndOrderWriteDoNotInterleaveAndLoseData() throws Exception {
        Long orderId = createOrder("快照并发债权", 2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?> snapshotFuture = executor.submit(() -> {
                await(start);
                snapshotService.rebuildDailySnapshots(LocalDate.of(2026, 10, 8));
                return null;
            });
            Future<?> addFuture = executor.submit(() -> {
                await(start);
                return addLoan(orderId, "100.00");
            });
            start.countDown();
            snapshotFuture.get(20, TimeUnit.SECONDS);
            assertThat(addFuture.get(20, TimeUnit.SECONDS)).isNotInstanceOf(Exception.class);
        } finally {
            executor.shutdownNow();
        }

        snapshotService.rebuildDailySnapshots(LocalDate.of(2026, 10, 8));
        BigDecimal balance = jdbcTemplate.queryForObject(
                "select balance from fortune_account where account_id = ?", BigDecimal.class, fixture.accountIdA());
        BigDecimal snapshot = jdbcTemplate.queryForObject("""
                select balance from fortune_balance_snapshot
                where account_id = ? and snapshot_date = ?
                """, BigDecimal.class, fixture.accountIdA(), LocalDate.of(2026, 10, 8));
        assertThat(snapshot).isEqualByComparingTo(balance);
    }

    private Long createOrder(String title, Integer type) {
        FinanceOrderCreateCommand command = new FinanceOrderCreateCommand();
        command.setBookId(fixture.bookIdA());
        command.setRequestId(UUID.randomUUID().toString());
        command.setTitle(title);
        command.setType(type);
        command.setCounterpartyId(type == 2 ? fixture.payeeId() : null);
        command.setCounterpartyName("张三");
        command.setRemark("");
        return requestService.create(command).getOrderId();
    }

    private FinanceOrderMutationVo addLoan(Long orderId, String amount) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(fixture.bookIdA());
        command.setOrderId(orderId);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(queryService.detail(fixture.bookIdA(), orderId).getVersion());
        command.setTitle("借出");
        command.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        command.setBillType(9);
        command.setOrderComponent("PRINCIPAL");
        command.setAmount(new BigDecimal(amount));
        command.setCategoryAmountPair(List.of());
        command.setAccountId(fixture.accountIdA());
        command.setConfirm(true);
        command.setInclude(false);
        return requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(orderId)
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(command)
                .requestId(command.getRequestId())
                .expectedVersion(command.getExpectedVersion())
                .fileList(List.of())
                .build());
    }

    private boolean objectDeleted(String table, String idColumn, Long id) {
        return jdbcTemplate.queryForObject(
                "select deleted from " + table + " where " + idColumn + " = ?", Boolean.class, id);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void login(Long userId) {
        SystemLoginUser loginUser = new SystemLoginUser(userId, false, "user" + userId, "", null);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
