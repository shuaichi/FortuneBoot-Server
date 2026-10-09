package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.service.fortune.FortuneBillService;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据写入SQLite真实事务")
@SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
class FinanceOrderTransactionTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage02-tx-");
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

    @Autowired
    private FortuneBillService fortuneBillService;

    private FinanceOrderSqliteTestSupport.Fixture fixture;

    private int orderType = 2;

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        this.fixture = FinanceOrderSqliteTestSupport.insertFixture(jdbcTemplate, 2);
    }

    @Test
    @DisplayName("债务本金多笔归还：余额和待还金额按C11案例2推进")
    void loanInPrincipalAndRepayment_updatesBalanceAndPending() {
        useOrderType(3);

        mutateAdd(11, "2000.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("12000.00");
        assertThat(pendingAmount()).isEqualByComparingTo("2000.00");

        mutateAdd(12, "500.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("11500.00");
        assertThat(pendingAmount()).isEqualByComparingTo("1500.00");

        mutateAdd(12, "1500.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(settlementState()).isEqualTo("SETTLED");
    }

    @Test
    @DisplayName("报销垫付多笔后到账：余额和待报销金额按C11案例3推进")
    void expenseClaimAdvanceAndReimburse_updatesBalanceAndPending() {
        useOrderType(1);

        mutateAdd(7, "800.00", fixture.accountIdA(), true);
        mutateAdd(7, "200.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
        assertThat(pendingAmount()).isEqualByComparingTo("1000.00");

        mutateAdd(8, "700.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9700.00");
        assertThat(pendingAmount()).isEqualByComparingTo("300.00");

        mutateAdd(8, "300.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
        assertThat(ledgerSnapshot().interestIncome()).isEqualByComparingTo("0.00");
        assertThat(ledgerSnapshot().feeAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("作废和恢复收回流水：现金按原确认状态回滚/重放，待收同步重建")
    void voidAndRestoreCollection_rebuildsCashAndPending() {
        mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        Long billId = mutateAdd(10, "400.00", fixture.accountIdA(), true).getBillId();

        mutateState(FinanceOrderEntryMutation.Operation.VOID, billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
        assertThat(pendingAmount()).isEqualByComparingTo("1000.00");

        mutateState(FinanceOrderEntryMutation.Operation.RESTORE, billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9400.00");
        assertThat(pendingAmount()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("利息和手续费单独记现金收支，不影响本金结清")
    void interestAndFee_updateCashAndOrderIncomeExpense() {
        mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        mutateAdd(10, "1000.00", fixture.accountIdA(), true);
        mutateAdd(2, FinanceOrderComponentEnum.INTEREST, "20.00", fixture.accountIdA(), true);
        mutateAdd(1, FinanceOrderComponentEnum.FEE, "5.00", fixture.accountIdA(), true);

        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10015.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(ledgerSnapshot().interestIncome()).isEqualByComparingTo("20.00");
        assertThat(ledgerSnapshot().feeAmount()).isEqualByComparingTo("5.00");
        assertThat(ledgerSnapshot().netCostOrIncome()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("报销差额结清不动现金，作废差额结清后待报销恢复")
    void writeOffForExpenseClaim_hasNoCashAndCanVoid() {
        useOrderType(1);
        mutateAdd(7, "1000.00", fixture.accountIdA(), true);
        mutateAdd(8, "800.00", fixture.accountIdA(), true);
        Long writeOffId = mutateAdd(1, FinanceOrderComponentEnum.WRITE_OFF, "200.00", null, true).getBillId();

        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");

        mutateState(FinanceOrderEntryMutation.Operation.VOID, writeOffId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("债权无法收回和债务免还均通过差额结清归零，不改变账户现金")
    void writeOffForLoanOutAndLoanIn_clearsPendingWithoutCash() {
        mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        mutateAdd(10, "800.00", fixture.accountIdA(), true);
        mutateAdd(1, FinanceOrderComponentEnum.WRITE_OFF, "200.00", null, true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9800.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(ledgerSnapshot().netCostOrIncome()).isEqualByComparingTo("-200.00");

        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        fixture = FinanceOrderSqliteTestSupport.insertFixture(jdbcTemplate, 3);
        orderType = 3;
        mutateAdd(11, "1000.00", fixture.accountIdA(), true);
        mutateAdd(12, "800.00", fixture.accountIdA(), true);
        mutateAdd(2, FinanceOrderComponentEnum.WRITE_OFF, "200.00", null, true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10200.00");
        assertThat(pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(ledgerSnapshot().netCostOrIncome()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("未确认本金不入现金；确认/取消确认重复操作无副作用，已被结算抵扣后取消整体失败")
    void unconfirmedPrincipal_confirmAndUnconfirmAreIdempotentAndProtectedBySettlement() {
        Long billId = mutateAdd(9, "1000.00", fixture.accountIdA(), false).getBillId();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
        assertThat(ledgerSnapshot().issuedAmount()).isEqualByComparingTo("0.00");
        assertThat(ledgerSnapshot().pendingEntryCount()).isEqualTo(1);

        mutateState(FinanceOrderEntryMutation.Operation.CONFIRM, billId);
        mutateState(FinanceOrderEntryMutation.Operation.CONFIRM, billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
        assertThat(ledgerSnapshot().pendingEntryCount()).isZero();

        mutateState(FinanceOrderEntryMutation.Operation.UNCONFIRM, billId);
        mutateState(FinanceOrderEntryMutation.Operation.UNCONFIRM, billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
        assertThat(ledgerSnapshot().pendingEntryCount()).isEqualTo(1);

        mutateState(FinanceOrderEntryMutation.Operation.CONFIRM, billId);
        mutateAdd(10, "400.00", fixture.accountIdA(), true);
        long versionBefore = orderLong("version", 0);
        BigDecimal balanceBefore = FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA());
        assertThatThrownBy(() -> mutateState(FinanceOrderEntryMutation.Operation.UNCONFIRM, billId))
                .isInstanceOf(Exception.class);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo(balanceBefore);
        assertThat(orderLong("version", 0)).isEqualTo(versionBefore);
    }

    @Test
    @DisplayName("已归档、跨分组账户、版本冲突和空单费用均拒绝且不产生现金/流水")
    void invalidMutations_rollBackAndLeaveNoCashOrBill() {
        jdbcTemplate.update("update fortune_finance_order set status = 1000 where order_id = ?", fixture.orderId());
        assertThatThrownBy(() -> mutateAdd(9, "1000.00", fixture.accountIdA(), true)).isInstanceOf(Exception.class);
        assertThat(count("fortune_bill")).isZero();

        jdbcTemplate.update("update fortune_finance_order set status = 100 where order_id = ?", fixture.orderId());
        jdbcTemplate.update("""
                insert into fortune_group(group_id, group_name, default_currency, enable, default_book_id, remark, deleted)
                values (910, '其他分组', 'CNY', 1, 0, '', 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (2001, 910, '跨组账户', 1, 1000.00, 'CNY', 1, 1, 1, 1, 0)
                """);
        assertThatThrownBy(() -> mutateAdd(9, "1000.00", 2001L, true)).isInstanceOf(Exception.class);
        assertThat(count("fortune_bill")).isZero();

        assertThatThrownBy(() -> mutateAdd(1, FinanceOrderComponentEnum.FEE, "5.00", fixture.accountIdA(), true))
                .isInstanceOf(Exception.class);
        assertThat(count("fortune_bill")).isZero();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");

        mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        long version = orderLong("version", 0);
        FinanceOrderEntryCommand command = entryCommand(10, FinanceOrderComponentEnum.PRINCIPAL, "400.00",
                fixture.accountIdA(), true);
        command.setExpectedVersion(0L);
        FinanceOrderEntryMutation mutation = mutation(FinanceOrderEntryMutation.Operation.ADD, command,
                UUID.randomUUID().toString());
        mutation.setExpectedVersion(0L);
        assertThatThrownBy(() -> requestService.mutateEntry(mutation)).isInstanceOf(Exception.class);
        assertThat(count("fortune_bill")).isEqualTo(1);
        assertThat(orderLong("version", 0)).isEqualTo(version);
    }

    @Test
    @DisplayName("停用旧账户允许冲正作废，但恢复必须要求账户可用并整体回滚")
    void disabledOldAccountAllowsVoidButRestoreFails() {
        Long billId = mutateAdd(9, "1000.00", fixture.accountIdA(), true).getBillId();
        jdbcTemplate.update("update fortune_account set enable = 0 where account_id = ?", fixture.accountIdA());

        mutateState(FinanceOrderEntryMutation.Operation.VOID, billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");

        assertThatThrownBy(() -> mutateState(FinanceOrderEntryMutation.Operation.RESTORE, billId))
                .isInstanceOf(Exception.class);
        assertThat(jdbcTemplate.queryForObject(
                "select recycle_bin from fortune_bill where bill_id = ?", Boolean.class, billId)).isTrue();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("旧FortuneBillService关联流水委托统一管线，remove转为作废且不双加余额")
    void legacyBillServiceDelegatesManagedEntryAndRemoveBecomesVoid() {
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setBookId(fixture.bookId());
        command.setOrderId(fixture.orderId());
        command.setTitle("旧入口借出");
        command.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        command.setAccountId(fixture.accountIdA());
        command.setBillType(9);
        command.setConfirm(true);
        command.setInclude(true);
        command.setAmount(new BigDecimal("1000.00"));
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal("1000.00"))));

        Long billId = fortuneBillService.add(command);
        assertThat(billId).isNotNull();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");

        fortuneBillService.confirm(fixture.bookId(), billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");

        fortuneBillService.exclude(fixture.bookId(), billId);
        assertThat(jdbcTemplate.queryForObject(
                "select include from fortune_bill where bill_id = ?", Boolean.class, billId)).isFalse();
        assertThat(jdbcTemplate.queryForObject(
                "select action from fortune_finance_order_audit where bill_id = ? order by id desc limit 1",
                String.class, billId)).isEqualTo("EXCLUDE");

        fortuneBillService.remove(fixture.bookId(), billId);
        assertThat(jdbcTemplate.queryForObject(
                "select recycle_bin from fortune_bill where bill_id = ?", Boolean.class, billId)).isTrue();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("债权本金多笔结算：余额和待收金额按C11案例1推进")
    void loanOutPrincipalAndSettlement_updatesBalanceAndPending() {
        FinanceOrderMutationVo first = mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        assertThat(first.getBillId()).isNotNull();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
        assertThat(pendingAmount()).isEqualByComparingTo("1000.00");

        mutateAdd(10, "400.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9400.00");
        assertThat(pendingAmount()).isEqualByComparingTo("600.00");

        mutateAdd(10, "600.00", fixture.accountIdA(), true);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
        assertThat(settlementState()).isEqualTo("SETTLED");
        assertThat(orderLong("version", 0)).isEqualTo(3L);
    }

    @Test
    @DisplayName("修改收回金额并换账户：原账户退款、目标账户入账、待收重建为700")
    void modifyCollection_withAccountChange_rebuildsBothAccountsAndLedger() {
        mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        Long billId = mutateAdd(10, "400.00", fixture.accountIdA(), true).getBillId();

        FinanceOrderMutationVo modified = mutateModify(billId, "300.00", fixture.accountIdB());

        assertThat(modified.getBillId()).isEqualTo(billId);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdB()))
                .isEqualByComparingTo("5300.00");
        assertThat(pendingAmount()).isEqualByComparingTo("700.00");
        assertThat(orderLong("version", 0)).isEqualTo(3L);
    }

    @Test
    @DisplayName("附件写入失败时流水、账户余额、单据版本、request和audit全部回滚")
    void fileFailure_rollsBackWholeMutation() {
        long beforeVersion = orderLong("version", 0);
        FinanceOrderEntryCommand command = entryCommand(9, FinanceOrderComponentEnum.PRINCIPAL, "1000.00",
                fixture.accountIdA(), true);
        FinanceOrderEntryMutation mutation = mutation(FinanceOrderEntryMutation.Operation.ADD, command, "file-failure");
        mutation.setFileList(List.of(new MockMultipartFile("file", "boom.txt", "text/plain", new byte[]{1})));

        jdbcTemplate.update("""
                create trigger fail_file_insert before insert on fortune_file
                when new.original_name = 'boom.txt'
                begin select raise(abort, 'file failure'); end
                """);
        try {
            assertThatThrownBy(() -> requestService.mutateEntry(mutation)).isInstanceOf(Exception.class);
        } finally {
            jdbcTemplate.update("drop trigger fail_file_insert");
        }

        assertThat(count("fortune_bill")).isZero();
        assertThat(count("fortune_file")).isZero();
        assertThat(count("fortune_finance_order_request")).isZero();
        assertThat(count("fortune_finance_order_audit")).isZero();
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("10000.00");
        assertThat(orderLong("version", 0)).isEqualTo(beforeVersion);
    }

    @Test
    @DisplayName("审计写入失败时同一事务内已写余额和流水全部回滚")
    void auditFailure_rollsBackAccountAndBillWrites() {
        mutateAdd(9, "1000.00", fixture.accountIdA(), true);
        long beforeVersion = orderLong("version", 0);
        jdbcTemplate.update("""
                create trigger fail_audit_insert before insert on fortune_finance_order_audit
                begin select raise(abort, 'audit failure'); end
                """);
        try {
            assertThatThrownBy(() -> mutateAdd(10, "400.00", fixture.accountIdA(), true))
                    .isInstanceOf(Exception.class);
        } finally {
            jdbcTemplate.update("drop trigger fail_audit_insert");
        }

        assertThat(count("fortune_bill")).isEqualTo(1);
        assertThat(count("fortune_finance_order_request")).isEqualTo(1);
        assertThat(count("fortune_finance_order_audit")).isEqualTo(1);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
        assertThat(orderLong("version", 0)).isEqualTo(beforeVersion);
    }

    private FinanceOrderMutationVo mutateAdd(int billType, String amount, Long accountId, boolean confirmed) {
        return mutateAdd(billType, FinanceOrderComponentEnum.PRINCIPAL, amount, accountId, confirmed);
    }

    private void useOrderType(int type) {
        this.orderType = type;
        jdbcTemplate.update("update fortune_finance_order set type = ? where order_id = ?", type, fixture.orderId());
    }

    private FinanceOrderMutationVo mutateAdd(int billType, FinanceOrderComponentEnum component,
                                             String amount, Long accountId, boolean confirmed) {
        return requestService.mutateEntry(mutation(FinanceOrderEntryMutation.Operation.ADD,
                entryCommand(billType, component, amount, accountId, confirmed),
                UUID.randomUUID().toString()));
    }

    private FinanceOrderMutationVo mutateState(FinanceOrderEntryMutation.Operation operation, Long billId) {
        FinanceOrderEntryMutation mutation = mutation(operation, null, UUID.randomUUID().toString());
        mutation.setBillId(billId);
        return requestService.mutateEntry(mutation);
    }

    private FinanceOrderMutationVo mutateModify(Long billId, String amount, Long accountId) {
        FinanceOrderEntryCommand command = entryCommand(10, FinanceOrderComponentEnum.PRINCIPAL, amount,
                accountId, true);
        FinanceOrderEntryMutation mutation = mutation(FinanceOrderEntryMutation.Operation.MODIFY, command,
                UUID.randomUUID().toString());
        mutation.setBillId(billId);
        return requestService.mutateEntry(mutation);
    }

    private FinanceOrderEntryCommand entryCommand(int billType, FinanceOrderComponentEnum component,
                                                   String amount, Long accountId, boolean confirmed) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(fixture.bookId());
        command.setOrderId(fixture.orderId());
        command.setTitle("阶段02流水");
        command.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        command.setAccountId(accountId);
        command.setBillType(billType);
        command.setOrderComponent(component.getValue());
        command.setConfirm(confirmed);
        command.setInclude(true);
        command.setAmount(new BigDecimal(amount));
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal(amount))));
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(orderLong("version", 0));
        return command;
    }

    private FinanceOrderEntryMutation mutation(FinanceOrderEntryMutation.Operation operation,
                                                FinanceOrderEntryCommand command, String requestId) {
        return FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .operation(operation)
                .entryCommand(command)
                .requestId(requestId)
                .expectedVersion(orderLong("version", 0))
                .build();
    }

    private long orderLong(String column, int expected) {
        Long value = jdbcTemplate.queryForObject(
                "select " + column + " from fortune_finance_order where order_id = ?", Long.class, fixture.orderId());
        assertThat(value).isNotNull();
        return value;
    }

    private BigDecimal pendingAmount() {
        return ledgerSnapshot().pendingAmount();
    }

    private String settlementState() {
        return ledgerSnapshot().settlementState();
    }

    private FinanceOrderLedgerSnapshot ledgerSnapshot() {
        return ledgerCalculator.calculate(orderType, ledgerEntries());
    }

    private List<FinanceOrderLedgerEntry> ledgerEntries() {
        return fortuneBillRepo.getByOrderId(fixture.orderId()).stream()
                .map(this::ledgerEntry)
                .toList();
    }

    private FinanceOrderLedgerEntry ledgerEntry(FortuneBillEntity bill) {
        return new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(), bill.getOrderComponent(),
                bill.getAmount(), bill.getTradeTime(), Boolean.TRUE.equals(bill.getConfirm()),
                Boolean.TRUE.equals(bill.getDeleted()), Boolean.TRUE.equals(bill.getRecycleBin()),
                bill.getOrderLedgerVersion() == null ? 0 : bill.getOrderLedgerVersion());
    }

    private String orderText(String column, int expected) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from fortune_finance_order where order_id = ?", String.class, fixture.orderId());
    }

    private int count(String table) {
        Integer value = jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
        assertThat(value).isNotNull();
        return value;
    }
}
