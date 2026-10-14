package com.fortuneboot.service.fortune.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据金额领域规则")
class FinanceOrderAmountRuleTest {

    @Test
    @DisplayName("MySQL DECIMAL尾零不构成额外小数，非零第三四位仍拒绝")
    void decimalScale4TrailingZeros_areValidButRealSubCentIsNot() {
        assertThat(FinanceOrderAmountRule.isValid(new BigDecimal("600.0000"))).isTrue();
        assertThat(FinanceOrderAmountRule.isValid(new BigDecimal("600.1000"))).isTrue();
        assertThat(FinanceOrderAmountRule.isValid(new BigDecimal("600.1010"))).isFalse();
        assertThat(FinanceOrderAmountRule.isValid(new BigDecimal("600.0010"))).isFalse();
    }
}
