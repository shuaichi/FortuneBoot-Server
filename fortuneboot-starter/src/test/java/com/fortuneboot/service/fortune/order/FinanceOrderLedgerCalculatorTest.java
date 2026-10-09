package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 单据账本纯计算器测试
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@DisplayName("单据账本纯计算器")
class FinanceOrderLedgerCalculatorTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 10, 1, 9, 0);

    private final FinanceOrderLedgerCalculator calculator = new FinanceOrderLedgerCalculator();

    @Test
    void receivable_repayment_doesNotEraseOutstandingPrincipal() {
        var entries = List.of(
            new FinanceOrderLedgerEntry(1L, 9, "PRINCIPAL", new BigDecimal("1000.00"),
                LocalDateTime.of(2026, 10, 1, 9, 0), true, false, false, 1),
            new FinanceOrderLedgerEntry(2L, 10, "PRINCIPAL", new BigDecimal("400.00"),
                LocalDateTime.of(2026, 10, 2, 9, 0), true, false, false, 1));
        var value = new FinanceOrderLedgerCalculator().calculate(2, entries);
        assertThat(value.pendingAmount()).isEqualByComparingTo("600.00");
        assertThat(value.settlementState()).isEqualTo("PARTIAL");
        assertThat(value.netCostOrIncome()).isEqualByComparingTo("0.00");
    }

    @Test
    void loanIn_tracksRepayDirection() {
        var entries = List.of(
            entry(1L, 11, "PRINCIPAL", "2000.00", 1, true),
            entry(2L, 12, "PRINCIPAL", "500.00", 2, true));

        var value = calculator.calculate(3, entries);

        assertThat(value.outAmount()).isEqualByComparingTo("500.00");
        assertThat(value.inAmount()).isEqualByComparingTo("2000.00");
        assertThat(value.issuedAmount()).isEqualByComparingTo("2000.00");
        assertThat(value.settledAmount()).isEqualByComparingTo("500.00");
        assertThat(value.pendingAmount()).isEqualByComparingTo("1500.00");
        assertThat(value.settlementState()).isEqualTo("PARTIAL");
    }

    @Test
    void expenseClaim_supportsMultipleAdvancesAndPartialReimburse() {
        var advances = List.of(
            entry(1L, 7, "PRINCIPAL", "800.00", 1, true),
            entry(2L, 7, "PRINCIPAL", "200.00", 2, true));

        var onlyAdvances = calculator.calculate(1, advances);
        assertThat(onlyAdvances.outAmount()).isEqualByComparingTo("1000.00");
        assertThat(onlyAdvances.inAmount()).isEqualByComparingTo("0.00");
        assertThat(onlyAdvances.pendingAmount()).isEqualByComparingTo("1000.00");
        assertThat(onlyAdvances.settlementState()).isEqualTo("UNSETTLED");

        var withReimburse = new ArrayList<>(advances);
        withReimburse.add(entry(3L, 8, "PRINCIPAL", "700.00", 3, true));

        var partial = calculator.calculate(1, withReimburse);
        assertThat(partial.pendingAmount()).isEqualByComparingTo("300.00");
        assertThat(partial.settlementState()).isEqualTo("PARTIAL");
    }

    @Test
    void writeOff_settlesExpenseClaimAndAffectsNetCost() {
        var entries = List.of(
            entry(1L, 7, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 8, "PRINCIPAL", "800.00", 2, true),
            entry(3L, 1, "WRITE_OFF", "200.00", 3, true));

        var value = calculator.calculate(1, entries);

        assertThat(value.adjustedAmount()).isEqualByComparingTo("200.00");
        assertThat(value.pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(value.settlementState()).isEqualTo("SETTLED");
        assertThat(value.netCostOrIncome()).isEqualByComparingTo("-200.00");
    }

    @Test
    void interestAndFee_doNotChangePendingAmount() {
        var entries = List.of(
            entry(1L, 9, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 10, "PRINCIPAL", "400.00", 2, true),
            entry(3L, 2, "INTEREST", "20.00", 3, true),
            entry(4L, 1, "FEE", "5.00", 3, true));

        var value = calculator.calculate(2, entries);

        assertThat(value.pendingAmount()).isEqualByComparingTo("600.00");
        assertThat(value.interestIncome()).isEqualByComparingTo("20.00");
        assertThat(value.interestExpense()).isEqualByComparingTo("0.00");
        assertThat(value.feeAmount()).isEqualByComparingTo("5.00");
        assertThat(value.netCostOrIncome()).isEqualByComparingTo("15.00");
    }

    @Test
    void loanInInterest_isExpenseDirection() {
        var entries = List.of(
            entry(1L, 11, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 12, "PRINCIPAL", "500.00", 2, true),
            entry(3L, 1, "INTEREST", "20.00", 3, true));

        var value = calculator.calculate(3, entries);

        assertThat(value.interestExpense()).isEqualByComparingTo("20.00");
        assertThat(value.interestIncome()).isEqualByComparingTo("0.00");
        assertThat(value.netCostOrIncome()).isEqualByComparingTo("-20.00");
    }

    @Test
    void unconfirmedEntry_isNotAccumulated_butCounted() {
        var onlyUnconfirmed = List.of(entry(1L, 9, "PRINCIPAL", "1000.00", 1, false));

        var empty = calculator.calculate(2, onlyUnconfirmed);
        assertThat(empty.outAmount()).isEqualByComparingTo("0.00");
        assertThat(empty.pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(empty.pendingEntryCount()).isEqualTo(1);
        assertThat(empty.settlementState()).isEqualTo("EMPTY");

        var withUnconfirmedSettle = List.of(
            entry(1L, 9, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 10, "PRINCIPAL", "400.00", 2, false));

        var value = calculator.calculate(2, withUnconfirmedSettle);
        assertThat(value.pendingAmount()).isEqualByComparingTo("1000.00");
        assertThat(value.pendingEntryCount()).isEqualTo(1);
    }

    @Test
    void voidedAndDeletedEntries_areIgnored() {
        var entries = List.of(
            new FinanceOrderLedgerEntry(1L, 9, "PRINCIPAL", new BigDecimal("1000.00"),
                BASE_TIME, true, false, true, 1),
            new FinanceOrderLedgerEntry(2L, 10, "PRINCIPAL", new BigDecimal("400.00"),
                BASE_TIME, false, true, false, 1));

        var value = calculator.calculate(2, entries);

        assertThat(value.outAmount()).isEqualByComparingTo("0.00");
        assertThat(value.inAmount()).isEqualByComparingTo("0.00");
        assertThat(value.pendingEntryCount()).isZero();
        assertThat(value.settlementState()).isEqualTo("EMPTY");
    }

    @Test
    void invalidAmounts_areRejected() {
        assertAmountRejected(null);
        assertAmountRejected(new BigDecimal("0"));
        assertAmountRejected(new BigDecimal("0.00"));
        assertAmountRejected(new BigDecimal("-1.00"));
        assertAmountRejected(new BigDecimal("1.234"));
        assertAmountRejected(new BigDecimal("1000000000000.00"));
    }

    @Test
    void unknownComponent_isRejected() {
        var entries = List.of(new FinanceOrderLedgerEntry(1L, 9, "UNKNOWN", new BigDecimal("1.00"),
            BASE_TIME, true, false, false, 1));

        assertThatThrownBy(() -> calculator.calculate(2, entries))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_REFERENCE_INVALID.code()));
    }

    @Test
    void unknownOrderType_isRejected() {
        assertThatThrownBy(() -> calculator.calculate(4, List.of()))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_TYPE_MISMATCH.code()));
    }

    @Test
    void combinationOutsideWhitelist_isRejected() {
        var loanOutBillOnExpenseClaim = List.of(entry(1L, 9, "PRINCIPAL", "100.00", 1, true));
        assertThatThrownBy(() -> calculator.calculate(1, loanOutBillOnExpenseClaim))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_TYPE_MISMATCH.code()));

        var payInterestOnLoanOut = List.of(entry(1L, 1, "INTEREST", "20.00", 1, true));
        assertThatThrownBy(() -> calculator.calculate(2, payInterestOnLoanOut))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_TYPE_MISMATCH.code()));
    }

    @Test
    void settleBeforeFormation_isRejected_evenIfFinalNotOverSettled() {
        var entries = List.of(
            entry(2L, 10, "PRINCIPAL", "600.00", 1, true),
            entry(1L, 9, "PRINCIPAL", "1000.00", 2, true));

        assertThatThrownBy(() -> calculator.calculate(2, entries))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID.code()));
    }

    @Test
    void overSettled_isRejected() {
        var entries = List.of(
            entry(1L, 9, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 10, "PRINCIPAL", "1500.00", 2, true));

        assertThatThrownBy(() -> calculator.calculate(2, entries))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_OVER_SETTLED.code()));
    }

    @Test
    void sameTimeEntries_sortByBillIdAscending() {
        var recoverBeforeLoanOut = List.of(
            entry(1L, 10, "PRINCIPAL", "600.00", 1, true),
            entry(2L, 9, "PRINCIPAL", "1000.00", 1, true));
        var reversedInput = recoverBeforeLoanOut.reversed();

        for (List<FinanceOrderLedgerEntry> entries : List.of(recoverBeforeLoanOut, reversedInput)) {
            assertThatThrownBy(() -> calculator.calculate(2, entries))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                    .isEqualTo(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID.code()));
        }

        var loanOutBeforeRecover = List.of(
            entry(1L, 9, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 10, "PRINCIPAL", "600.00", 1, true));
        assertThat(calculator.calculate(2, loanOutBeforeRecover).pendingAmount())
            .isEqualByComparingTo("400.00");
    }

    @Test
    void nullBillId_isSortedLast() {
        var newEntryAfterExistingBill = List.of(
            entry(1L, 9, "PRINCIPAL", "1000.00", 1, true),
            entry(null, 10, "PRINCIPAL", "600.00", 1, true));
        assertThat(calculator.calculate(2, newEntryAfterExistingBill).pendingAmount())
            .isEqualByComparingTo("400.00");

        var newSettleBeforeFormation = List.of(
            entry(null, 10, "PRINCIPAL", "600.00", 1, true),
            entry(1L, 9, "PRINCIPAL", "1000.00", 2, true));
        assertThatThrownBy(() -> calculator.calculate(2, newSettleBeforeFormation))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID.code()));
    }

    @Test
    void zeroPrincipalWithFeeOrInterest_isRejected() {
        var feeOnly = List.of(entry(1L, 1, "FEE", "5.00", 1, true));
        assertThatThrownBy(() -> calculator.calculate(2, feeOnly))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID.code()));

        var interestOnly = List.of(entry(1L, 1, "INTEREST", "20.00", 1, true));
        assertThatThrownBy(() -> calculator.calculate(3, interestOnly))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID.code()));
    }

    @Test
    void versionZeroEntries_areNeverEnabled() {
        var entries = List.of(
            new FinanceOrderLedgerEntry(1L, 9, "PRINCIPAL", new BigDecimal("1000.00"),
                BASE_TIME, true, false, false, 0),
            new FinanceOrderLedgerEntry(2L, 10, "PRINCIPAL", new BigDecimal("-400.00"),
                BASE_TIME, true, false, false, 0));

        var value = calculator.calculate(2, entries);

        assertThat(value.outAmount()).isEqualByComparingTo("0.00");
        assertThat(value.inAmount()).isEqualByComparingTo("0.00");
        assertThat(value.pendingEntryCount()).isZero();
        assertThat(value.settlementState()).isEqualTo("EMPTY");
    }

    @Test
    void inputOrder_doesNotChangeResult() {
        var entries = List.of(
            entry(1L, 9, "PRINCIPAL", "1000.00", 1, true),
            entry(2L, 10, "PRINCIPAL", "400.00", 2, true),
            entry(3L, 2, "INTEREST", "20.00", 3, true),
            entry(4L, 1, "FEE", "5.00", 3, true));

        var expected = calculator.calculate(2, entries);
        var shuffled = List.copyOf(entries.reversed());

        var actual = calculator.calculate(2, shuffled);

        assertThat(actual.outAmount()).isEqualByComparingTo(expected.outAmount());
        assertThat(actual.inAmount()).isEqualByComparingTo(expected.inAmount());
        assertThat(actual.pendingAmount()).isEqualByComparingTo(expected.pendingAmount());
        assertThat(actual.interestIncome()).isEqualByComparingTo(expected.interestIncome());
        assertThat(actual.feeAmount()).isEqualByComparingTo(expected.feeAmount());
        assertThat(actual.netCostOrIncome()).isEqualByComparingTo(expected.netCostOrIncome());
        assertThat(actual.pendingEntryCount()).isEqualTo(expected.pendingEntryCount());
        assertThat(actual.settlementState()).isEqualTo(expected.settlementState());
    }

    @Test
    void aggregatedAmountOverDatabaseCapacity_isRejected() {
        List<FinanceOrderLedgerEntry> entries = new ArrayList<>();
        for (long billId = 1; billId <= 10001; billId++) {
            entries.add(entry(billId, 9, "PRINCIPAL", "999999999999.99", 1, true));
        }

        assertThatThrownBy(() -> calculator.calculate(2, entries))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_AMOUNT_INVALID.code()));
    }

    @Test
    void nullTradeTime_isRejected() {
        var entries = List.of(new FinanceOrderLedgerEntry(1L, 9, "PRINCIPAL", new BigDecimal("1000.00"),
            null, true, false, false, 1));

        assertThatThrownBy(() -> calculator.calculate(2, entries))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_CHRONOLOGY_INVALID.code()));
    }

    @Test
    void nullEntries_behaveAsEmpty() {
        var value = calculator.calculate(1, null);

        assertThat(value.outAmount()).isEqualByComparingTo("0.00");
        assertThat(value.inAmount()).isEqualByComparingTo("0.00");
        assertThat(value.pendingAmount()).isEqualByComparingTo("0.00");
        assertThat(value.netCostOrIncome()).isEqualByComparingTo("0.00");
        assertThat(value.pendingEntryCount()).isZero();
        assertThat(value.settlementState()).isEqualTo("EMPTY");
    }

    private void assertAmountRejected(BigDecimal amount) {
        var entries = List.of(new FinanceOrderLedgerEntry(1L, 9, "PRINCIPAL", amount,
            BASE_TIME, true, false, false, 1));

        assertThatThrownBy(() -> calculator.calculate(2, entries))
            .isInstanceOf(ApiException.class)
            .satisfies(e -> assertThat(((ApiException) e).getErrorCode().code())
                .isEqualTo(ErrorCode.Business.ORDER_AMOUNT_INVALID.code()));
    }

    private static FinanceOrderLedgerEntry entry(Long billId, int billType, String component,
                                                 String amount, int tradeDay, boolean confirmed) {
        return new FinanceOrderLedgerEntry(billId, billType, component, new BigDecimal(amount),
            LocalDateTime.of(2026, 10, tradeDay, 9, 0), confirmed, false, false, 1);
    }
}
