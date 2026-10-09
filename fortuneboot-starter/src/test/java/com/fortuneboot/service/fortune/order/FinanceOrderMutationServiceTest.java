package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据写入纯规则")
class FinanceOrderMutationServiceTest {

    private final FinanceOrderCashEffectCalculator calculator = new FinanceOrderCashEffectCalculator();

    @Test
    @DisplayName("现金效果只作用于有效version1组件，WRITE_OFF和legacy借贷为空")
    void cashEffect_supportsOnlyEffectiveVersionOneComponents() {
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT, FinanceOrderComponentEnum.PRINCIPAL, "100.00", true, false, false, 1)))
                .containsEntry(10L, new BigDecimal("-100.00"));
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_RECOVER, FinanceOrderComponentEnum.PRINCIPAL, "100.00", true, false, false, 1)))
                .containsEntry(10L, new BigDecimal("100.00"));
        assertThat(calculator.calculate(cashBill(BillTypeEnum.INCOME, FinanceOrderComponentEnum.INTEREST, "20.00", true, false, false, 1)))
                .containsEntry(10L, new BigDecimal("20.00"));
        assertThat(calculator.calculate(cashBill(BillTypeEnum.EXPENSE, FinanceOrderComponentEnum.FEE, "5.00", true, false, false, 1)))
                .containsEntry(10L, new BigDecimal("-5.00"));
        assertThat(calculator.calculate(cashBill(BillTypeEnum.EXPENSE, FinanceOrderComponentEnum.WRITE_OFF, "5.00", true, false, false, 1))).isEmpty();
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT, FinanceOrderComponentEnum.PRINCIPAL, "100.00", true, false, false, 0))).isEmpty();
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT, null, "100.00", true, false, false, 1))).isEmpty();
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT, FinanceOrderComponentEnum.PRINCIPAL, "100.00", false, false, false, 1))).isEmpty();
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT, FinanceOrderComponentEnum.PRINCIPAL, "100.00", true, true, false, 1))).isEmpty();
        assertThat(calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT, FinanceOrderComponentEnum.PRINCIPAL, "100.00", true, false, true, 1))).isEmpty();
    }

    @Test
    @DisplayName("现金效果金额必须为正数且最多两位小数")
    void cashEffect_rejectsInvalidAmounts() {
        assertThatThrownBy(() -> calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT,
                FinanceOrderComponentEnum.PRINCIPAL, "-1.00", true, false, false, 1)))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.Business.ORDER_AMOUNT_INVALID);
        assertThatThrownBy(() -> calculator.calculate(cashBill(BillTypeEnum.LOAN_OUT,
                FinanceOrderComponentEnum.PRINCIPAL, "1.001", true, false, false, 1)))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.Business.ORDER_AMOUNT_INVALID);
    }

    @Test
    @DisplayName("账户增量按账户ID升序合并，换账户新旧两侧都参与")
    void mergeDeltas_byAscendingAccountId() {
        Map<Long, BigDecimal> merged = FinanceOrderEntryMutator.mergeDeltas(
                Map.of(10L, new BigDecimal("100.00"), 20L, new BigDecimal("-30.00")),
                Map.of(10L, new BigDecimal("-40.00"), 30L, new BigDecimal("8.00")));

        assertThat(merged).containsOnlyKeys(10L, 20L, 30L);
        assertThat(merged.get(10L)).isEqualByComparingTo("-140.00");
        assertThat(merged.get(20L)).isEqualByComparingTo("30.00");
        assertThat(merged.get(30L)).isEqualByComparingTo("8.00");
    }

    private static FortuneBillEntity cashBill(BillTypeEnum billType, FinanceOrderComponentEnum component,
                                               String amount, boolean confirmed, boolean deleted,
                                               boolean voided, int ledgerVersion) {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBillType(billType.getValue());
        bill.setOrderComponent(component == null ? null : component.getValue());
        bill.setAmount(new BigDecimal(amount));
        bill.setAccountId(10L);
        bill.setConfirm(confirmed);
        bill.setDeleted(deleted);
        bill.setRecycleBin(voided);
        bill.setOrderLedgerVersion(ledgerVersion);
        bill.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        return bill;
    }
}
