package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.service.fortune.FortuneBalanceSnapshotService;
import com.fortuneboot.service.fortune.snapshot.FortuneBillBalanceDeltaCalculator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据余额快照定向重建")
@SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
class FinanceOrderSnapshotTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage02-snapshot-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    private final FortuneBillBalanceDeltaCalculator calculator = new FortuneBillBalanceDeltaCalculator();

    @Autowired
    private FortuneBalanceSnapshotService snapshotService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        FinanceOrderSqliteTestSupport.insertFixture(jdbcTemplate, 2);
    }

    @Test
    @DisplayName("定向重建只替换目标账户日期范围内的快照，不触碰未涉及账户与更早快照")
    void targetedRebuild_replacesOnlyTargetAccountRange() {
        jdbcTemplate.update("""
                insert into fortune_bill(bill_id,book_id,title,trade_time,account_id,amount,converted_amount,
                    order_id,bill_type,confirm,include,recycle_bin,order_component,order_ledger_version,deleted)
                values (1,901,'A借出','2026-10-01 09:00:00',1001,100.00,100.00,1,9,1,1,0,'PRINCIPAL',1,0)
                """);
        jdbcTemplate.update("update fortune_account set balance = 9900.00 where account_id = 1001");
        insertSnapshot(1001, "2026-09-30", "10000.00");
        insertSnapshot(1001, "2026-10-01", "10000.00");
        insertSnapshot(1002, "2026-10-01", "5000.00");

        snapshotService.rebuildDailySnapshots(List.of(1001L),
                LocalDate.of(2026, 10, 8), LocalDate.of(2026, 10, 1));

        assertThat(snapshotCount(1001L, "2026-09-30")).isEqualTo(1);
        assertThat(snapshotCount(1001L, "2026-10-01")).isEqualTo(1);
        assertThat(snapshotCount(1001L, "2026-10-08")).isEqualTo(1);
        assertThat(snapshotCount(1001L, "2026-10-09")).isZero();
        assertThat(snapshotCount(1002L, "2026-10-01")).isEqualTo(1);
        assertThat(snapshotBalance(1001L, "2026-10-08")).isEqualByComparingTo("9900.00");
        assertThat(snapshotBalance(1002L, "2026-10-01")).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("快照计算器拆分legacy0与new1借贷，version1现金参与，WRITE_OFF为0")
    void deltaCalculator_splitsLegacyAndNewLoanEffects() {
        assertThat(calculator.calculate(loan(BillTypeEnum.LOAN_OUT, 0), List.of())).isEmpty();
        assertThat(calculator.calculate(loan(BillTypeEnum.LOAN_RECOVER, 0), List.of())).isEmpty();

        assertThat(calculator.calculate(loan(BillTypeEnum.LOAN_OUT, 1), List.of()))
                .extracting(change -> change.amount())
                .containsExactly(new BigDecimal("-100.00"));
        assertThat(calculator.calculate(loan(BillTypeEnum.LOAN_RECOVER, 1), List.of()))
                .extracting(change -> change.amount())
                .containsExactly(new BigDecimal("100.00"));
        assertThat(calculator.calculate(loan(BillTypeEnum.LOAN_OUT, 1), List.of()))
                .extracting(com.fortuneboot.service.fortune.snapshot.AccountBalanceChange::accountId)
                .containsExactly(10L);
    }

    private static FortuneBillEntity loan(BillTypeEnum billType, int ledgerVersion) {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBillType(billType.getValue());
        bill.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        bill.setOrderLedgerVersion(ledgerVersion);
        bill.setAccountId(10L);
        bill.setAmount(new BigDecimal("100.00"));
        bill.setConfirm(true);
        bill.setDeleted(false);
        bill.setRecycleBin(false);
        bill.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        return bill;
    }

    private void insertSnapshot(long accountId, String snapshotDate, String balance) {
        jdbcTemplate.update("""
                insert into fortune_balance_snapshot(group_id,book_id,account_id,snapshot_date,currency_code,
                    balance,converted_balance,total_assets,total_liabilities,net_assets,deleted)
                values (900,901,?,?,'CNY',?,?,0,0,0,0)
                """, accountId, snapshotDate, balance, balance);
    }

    private int snapshotCount(long accountId, String snapshotDate) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(*) from fortune_balance_snapshot
                where account_id = ? and snapshot_date = ? and deleted = 0
                """, Integer.class, accountId, snapshotDate);
        assertThat(count).isNotNull();
        return count;
    }

    private BigDecimal snapshotBalance(long accountId, String snapshotDate) {
        BigDecimal balance = jdbcTemplate.queryForObject("""
                select balance from fortune_balance_snapshot
                where account_id = ? and snapshot_date = ? and deleted = 0
                """, BigDecimal.class, accountId, snapshotDate);
        assertThat(balance).isNotNull();
        return balance;
    }
}
