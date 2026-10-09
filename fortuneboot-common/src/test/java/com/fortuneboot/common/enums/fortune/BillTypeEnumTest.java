package com.fortuneboot.common.enums.fortune;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.fortuneboot.common.enums.fortune.BillTypeEnum.ADVANCE;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.EXPENSE;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.LOSS;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.LOAN_IN;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.LOAN_OUT;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.LOAN_RECOVER;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.LOAN_REPAY;
import static com.fortuneboot.common.enums.fortune.BillTypeEnum.REIMBURSE;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("账单流水类型领域语义")
class BillTypeEnumTest {

    @Test
    @DisplayName("合法值由枚举遍历生成且空值/未知值非法")
    void validValuesAreDerivedFromEnumConstants() {
        assertThat(BillTypeEnum.validValues()).hasSize(12);
        assertThat(BillTypeEnum.validValues()).contains(EXPENSE.getValue(), LOAN_REPAY.getValue());
        assertThat(BillTypeEnum.isValidValue(null)).isFalse();
        assertThat(BillTypeEnum.isValidValue(0)).isFalse();
        assertThat(BillTypeEnum.isValidValue(13)).isFalse();
    }

    @Test
    @DisplayName("单据形成动作只包含垫付、借出、借入")
    void orderFormationTypesAreAdvanceLoanOutAndLoanIn() {
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isOrderFormation)
                .containsExactlyInAnyOrder(ADVANCE, LOAN_OUT, LOAN_IN);
        assertThat(BillTypeEnum.isOrderFormationValue(ADVANCE.getValue())).isTrue();
        assertThat(BillTypeEnum.isOrderFormationValue(REIMBURSE.getValue())).isFalse();
    }

    @Test
    @DisplayName("单据结算动作只包含报销、收回、归还")
    void orderSettlementTypesAreReimburseRecoverAndRepay() {
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isOrderSettlement)
                .containsExactlyInAnyOrder(REIMBURSE, LOAN_RECOVER, LOAN_REPAY);
        assertThat(BillTypeEnum.isOrderSettlementValue(LOAN_REPAY.getValue())).isTrue();
        assertThat(BillTypeEnum.isOrderSettlementValue(LOAN_IN.getValue())).isFalse();
    }

    @Test
    @DisplayName("本金出向和入向类型互不重叠")
    void principalDirectionsDoNotOverlap() {
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isPrincipalOut)
                .containsExactlyInAnyOrder(ADVANCE, LOAN_OUT, LOAN_REPAY);
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isPrincipalIn)
                .containsExactlyInAnyOrder(REIMBURSE, LOAN_RECOVER, LOAN_IN);
        assertThat(BillTypeEnum.isPrincipalOutValue(ADVANCE.getValue())).isTrue();
        assertThat(BillTypeEnum.isPrincipalInValue(ADVANCE.getValue())).isFalse();
    }

    @Test
    @DisplayName("报销类型包含垫付和报销")
    void claimTypesAreAdvanceAndReimburse() {
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isClaimType)
                .containsExactlyInAnyOrder(ADVANCE, REIMBURSE);
        assertThat(BillTypeEnum.isClaimValue(REIMBURSE.getValue())).isTrue();
        assertThat(BillTypeEnum.isClaimValue(LOAN_OUT.getValue())).isFalse();
    }

    @Test
    @DisplayName("借贷类型包含借出、收回、借入、归还")
    void loanTypesAreFourLoanBillTypes() {
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isLoanType)
                .containsExactlyInAnyOrder(LOAN_OUT, LOAN_RECOVER, LOAN_IN, LOAN_REPAY);
        assertThat(BillTypeEnum.loanTypeValues())
                .containsExactlyInAnyOrder(LOAN_OUT.getValue(), LOAN_RECOVER.getValue(),
                        LOAN_IN.getValue(), LOAN_REPAY.getValue());
        assertThat(BillTypeEnum.isLoanValue(null)).isFalse();
        assertThat(BillTypeEnum.isLoanValue(LOAN_REPAY.getValue())).isTrue();
    }

    @Test
    @DisplayName("利息收入对应收入流水，利息支出对应支出流水")
    void interestDirectionIsDerivedFromIncomeAndExpense() {
        assertThat(BillTypeEnum.isInterestIncomeValue(BillTypeEnum.INCOME.getValue())).isTrue();
        assertThat(BillTypeEnum.isInterestIncomeValue(EXPENSE.getValue())).isFalse();
        assertThat(BillTypeEnum.isInterestExpenseValue(EXPENSE.getValue())).isTrue();
        assertThat(BillTypeEnum.isInterestExpenseValue(BillTypeEnum.INCOME.getValue())).isFalse();
    }

    @Test
    @DisplayName("单据托管类型由报销与借贷枚举推导，禁止手写7到12区间")
    void orderManagedTypesAreDerivedFromClaimAndLoanEnums() {
        assertThat(BillTypeEnum.values())
                .filteredOn(BillTypeEnum::isOrderManaged)
                .containsExactlyInAnyOrder(ADVANCE, REIMBURSE, LOAN_OUT, LOAN_RECOVER, LOAN_IN, LOAN_REPAY);
        assertThat(BillTypeEnum.orderManagedValues())
                .containsExactlyInAnyOrder(ADVANCE.getValue(), REIMBURSE.getValue(), LOAN_OUT.getValue(),
                        LOAN_RECOVER.getValue(), LOAN_IN.getValue(), LOAN_REPAY.getValue());
        assertThat(BillTypeEnum.isOrderManagedValue(LOAN_REPAY.getValue())).isTrue();
        assertThat(BillTypeEnum.isOrderManagedValue(LOSS.getValue())).isFalse();
        assertThat(BillTypeEnum.isOrderManagedValue(null)).isFalse();
    }
}
