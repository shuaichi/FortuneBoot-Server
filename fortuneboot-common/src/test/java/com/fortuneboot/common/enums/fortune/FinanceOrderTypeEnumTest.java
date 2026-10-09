package com.fortuneboot.common.enums.fortune;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum.EXPENSE_CLAIM;
import static com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum.LOAN_IN;
import static com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum.LOAN_OUT;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据类型领域语义")
class FinanceOrderTypeEnumTest {

    @Test
    @DisplayName("根据编码查询枚举且未知编码返回空")
    void getByValueReturnsNullForUnknownValue() {
        assertThat(FinanceOrderTypeEnum.getByValue(EXPENSE_CLAIM.getValue())).isEqualTo(EXPENSE_CLAIM);
        assertThat(FinanceOrderTypeEnum.getByValue(null)).isNull();
        assertThat(FinanceOrderTypeEnum.getByValue(99)).isNull();
    }

    @Test
    @DisplayName("流水类型显式映射到单据类型")
    void fromBillTypeMapsOrderTypesExplicitly() {
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.ADVANCE)).isEqualTo(EXPENSE_CLAIM);
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.REIMBURSE)).isEqualTo(EXPENSE_CLAIM);
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.LOAN_OUT)).isEqualTo(LOAN_OUT);
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.LOAN_RECOVER)).isEqualTo(LOAN_OUT);
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.LOAN_IN)).isEqualTo(LOAN_IN);
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.LOAN_REPAY)).isEqualTo(LOAN_IN);
        assertThat(FinanceOrderTypeEnum.fromBillType(BillTypeEnum.EXPENSE)).isNull();
        assertThat(FinanceOrderTypeEnum.fromBillType(null)).isNull();
    }

    @Test
    @DisplayName("组件与流水类型白名单由单据类型维护")
    void allowsValidatesComponentAndBillTypePairs() {
        assertThat(EXPENSE_CLAIM.allows(FinanceOrderComponentEnum.PRINCIPAL, BillTypeEnum.ADVANCE)).isTrue();
        assertThat(EXPENSE_CLAIM.allows(FinanceOrderComponentEnum.PRINCIPAL, BillTypeEnum.REIMBURSE)).isTrue();
        assertThat(EXPENSE_CLAIM.allows(FinanceOrderComponentEnum.PRINCIPAL, BillTypeEnum.LOAN_OUT)).isFalse();
        assertThat(EXPENSE_CLAIM.allows(FinanceOrderComponentEnum.FEE, BillTypeEnum.EXPENSE)).isTrue();
        assertThat(EXPENSE_CLAIM.allows(FinanceOrderComponentEnum.INTEREST, BillTypeEnum.INCOME)).isFalse();
        assertThat(LOAN_OUT.allows(FinanceOrderComponentEnum.INTEREST, BillTypeEnum.INCOME)).isTrue();
        assertThat(LOAN_IN.allows(FinanceOrderComponentEnum.INTEREST, BillTypeEnum.EXPENSE)).isTrue();
        assertThat(LOAN_IN.allows(FinanceOrderComponentEnum.WRITE_OFF, BillTypeEnum.INCOME)).isTrue();
        assertThat(LOAN_IN.allows(FinanceOrderComponentEnum.WRITE_OFF, BillTypeEnum.EXPENSE)).isFalse();
    }

    @Test
    @DisplayName("草稿入口动作由单据类型维护")
    void initialEntryActionsFollowApiContract() {
        assertThat(EXPENSE_CLAIM.initialEntryActions())
                .containsExactly(FinanceOrderEntryActionEnum.ADVANCE);
        assertThat(LOAN_OUT.initialEntryActions())
                .containsExactly(FinanceOrderEntryActionEnum.LOAN_OUT);
        assertThat(LOAN_IN.initialEntryActions())
                .containsExactly(FinanceOrderEntryActionEnum.LOAN_IN);
    }

    @Test
    @DisplayName("进行中入口动作由单据类型维护且符合接口契约")
    void usingEntryActionsFollowApiContract() {
        assertThat(EXPENSE_CLAIM.usingEntryActions()).containsExactly(
                FinanceOrderEntryActionEnum.ADVANCE, FinanceOrderEntryActionEnum.REIMBURSE,
                FinanceOrderEntryActionEnum.FEE, FinanceOrderEntryActionEnum.WRITE_OFF);
        assertThat(LOAN_OUT.usingEntryActions()).containsExactly(
                FinanceOrderEntryActionEnum.LOAN_OUT, FinanceOrderEntryActionEnum.LOAN_RECOVER,
                FinanceOrderEntryActionEnum.INTEREST_IN, FinanceOrderEntryActionEnum.FEE,
                FinanceOrderEntryActionEnum.WRITE_OFF);
        assertThat(LOAN_IN.usingEntryActions()).containsExactly(
                FinanceOrderEntryActionEnum.LOAN_IN, FinanceOrderEntryActionEnum.LOAN_REPAY,
                FinanceOrderEntryActionEnum.INTEREST_OUT, FinanceOrderEntryActionEnum.FEE,
                FinanceOrderEntryActionEnum.WRITE_OFF);
    }
}
