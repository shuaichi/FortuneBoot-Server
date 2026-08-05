package com.fortuneboot.service.fortune.snapshot;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneBillBalanceDeltaCalculatorTest {

    private final FortuneBillBalanceDeltaCalculator calculator = new FortuneBillBalanceDeltaCalculator();

    @Test
    @DisplayName("支出按金额加手续费减优惠扣减账户余额")
    void calculate_expenseWithFeeAndDiscount_returnsNegativeActualAmount() {
        FortuneBillEntity bill = bill(BillTypeEnum.EXPENSE, 10L, null, "100.00", null);
        List<FortuneBillExtraEntity> extras = List.of(
                extra(1, null, "5.00"),
                extra(2, null, "10.00")
        );

        List<AccountBalanceChange> changes = calculator.calculate(bill, extras);

        assertThat(changes).containsExactly(change(10L, "-95.00"));
    }

    @Test
    @DisplayName("收入增加账户余额")
    void calculate_income_returnsPositiveAmount() {
        FortuneBillEntity bill = bill(BillTypeEnum.INCOME, 10L, null, "200.00", null);

        List<AccountBalanceChange> changes = calculator.calculate(bill, List.of());

        assertThat(changes).containsExactly(change(10L, "200.00"));
    }

    @Test
    @DisplayName("转账分别计算转出账户扣款和转入账户入账")
    void calculate_transfer_returnsFromAndToChanges() {
        FortuneBillEntity bill = bill(BillTypeEnum.TRANSFER, 10L, 20L, "100.00", "98.00");
        List<FortuneBillExtraEntity> extras = List.of(
                extra(1, 1, "3.00"),
                extra(2, 1, "1.00"),
                extra(1, 2, "2.00"),
                extra(2, 2, "4.00")
        );

        List<AccountBalanceChange> changes = calculator.calculate(bill, extras);

        assertThat(changes).containsExactly(
                change(10L, "-102.00"),
                change(20L, "100.00")
        );
    }

    @Test
    @DisplayName("垫付减少账户余额，报销和盈利增加账户余额")
    void calculate_advanceReimburseProfit_returnsExpectedDirections() {
        assertThat(calculator.calculate(bill(BillTypeEnum.ADVANCE, 10L, null, "50.00", null), List.of()))
                .containsExactly(change(10L, "-50.00"));
        assertThat(calculator.calculate(bill(BillTypeEnum.REIMBURSE, 10L, null, "50.00", null), List.of()))
                .containsExactly(change(10L, "50.00"));
        assertThat(calculator.calculate(bill(BillTypeEnum.PROFIT, 10L, null, "30.00", null), List.of()))
                .containsExactly(change(10L, "30.00"));
    }

    @Test
    @DisplayName("余额调整使用差额作为账户余额变动")
    void calculate_adjust_returnsAmountAsDelta() {
        FortuneBillEntity bill = bill(BillTypeEnum.ADJUST, 10L, null, "123.00", "123.00");

        List<AccountBalanceChange> changes = calculator.calculate(bill, List.of());

        assertThat(changes).containsExactly(change(10L, "123.00"));
    }

    @Test
    @DisplayName("余额调整脏数据 amount 为零时优先使用 convertedAmount 修复")
    void calculate_adjustZeroAmount_fallsBackToConvertedAmount() {
        FortuneBillEntity bill = bill(BillTypeEnum.ADJUST, 10L, null, "0.00", "123.00");
        bill.setBillId(99L);
        bill.setTradeTime(LocalDateTime.of(2026, 5, 14, 10, 0));

        List<AccountBalanceChange> changes = calculator.calculate(bill, List.of());

        assertThat(changes).containsExactly(change(10L, LocalDate.of(2026, 5, 14), "123.00"));
    }

    @Test
    @DisplayName("无法修复的余额调整脏数据不阻塞计算")
    void calculate_adjustZeroAmountAndConvertedAmount_returnsNoChange() {
        FortuneBillEntity bill = bill(BillTypeEnum.ADJUST, 10L, null, "0.00", "0.00");
        bill.setBillId(99L);
        bill.setTradeTime(LocalDateTime.of(2026, 5, 14, 10, 0));

        List<AccountBalanceChange> changes = calculator.calculate(bill, List.of());

        assertThat(changes).isEmpty();
    }

    @Test
    @DisplayName("亏损和借贷类型不影响账户余额")
    void calculate_lossAndLoanTypes_returnNoChanges() {
        assertThat(calculator.calculate(bill(BillTypeEnum.LOSS, 10L, null, "30.00", null), List.of())).isEmpty();
        assertThat(calculator.calculate(bill(BillTypeEnum.LOAN_OUT, 10L, null, "30.00", null), List.of())).isEmpty();
        assertThat(calculator.calculate(bill(BillTypeEnum.LOAN_RECOVER, 10L, null, "30.00", null), List.of())).isEmpty();
        assertThat(calculator.calculate(bill(BillTypeEnum.LOAN_IN, 10L, null, "30.00", null), List.of())).isEmpty();
        assertThat(calculator.calculate(bill(BillTypeEnum.LOAN_REPAY, 10L, null, "30.00", null), List.of())).isEmpty();
    }

    private static FortuneBillEntity bill(BillTypeEnum type, Long accountId, Long toAccountId, String amount, String convertedAmount) {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBillType(type.getValue());
        bill.setAccountId(accountId);
        bill.setToAccountId(toAccountId);
        bill.setAmount(decimal(amount));
        bill.setConvertedAmount(convertedAmount == null ? null : decimal(convertedAmount));
        bill.setTradeTime(LocalDateTime.of(2026, 8, 4, 12, 0));
        return bill;
    }

    private static FortuneBillExtraEntity extra(Integer extraType, Integer accountSide, String amount) {
        FortuneBillExtraEntity extra = new FortuneBillExtraEntity();
        extra.setExtraType(extraType);
        extra.setAccountSide(accountSide);
        extra.setAmount(decimal(amount));
        return extra;
    }

    private static AccountBalanceChange change(Long accountId, String amount) {
        return change(accountId, LocalDate.of(2026, 8, 4), amount);
    }

    private static AccountBalanceChange change(Long accountId, LocalDate tradeDate, String amount) {
        return new AccountBalanceChange(accountId, tradeDate, decimal(amount));
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
