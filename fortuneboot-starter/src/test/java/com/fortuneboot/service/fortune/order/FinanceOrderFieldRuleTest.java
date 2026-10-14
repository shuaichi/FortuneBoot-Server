package com.fortuneboot.service.fortune.order;

import com.fortuneboot.domain.rule.fortune.order.FinanceOrderFieldRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据字段领域规则")
class FinanceOrderFieldRuleTest {

    @Test
    @DisplayName("标题必须非空白且不超过50个字符")
    void title_requiresNonBlankAndLimitedLength() {
        assertThat(FinanceOrderFieldRule.isValidTitle("报销单")).isTrue();
        assertThat(FinanceOrderFieldRule.isValidTitle("a".repeat(50))).isTrue();
        assertThat(FinanceOrderFieldRule.isValidTitle("")).isFalse();
        assertThat(FinanceOrderFieldRule.isValidTitle("   ")).isFalse();
        assertThat(FinanceOrderFieldRule.isValidTitle("a".repeat(51))).isFalse();
    }

    @Test
    @DisplayName("备注允许为空但不能超过512个字符")
    void remark_allowsEmptyButLimitsLength() {
        assertThat(FinanceOrderFieldRule.isValidRemark("")).isTrue();
        assertThat(FinanceOrderFieldRule.isValidRemark("a".repeat(512))).isTrue();
        assertThat(FinanceOrderFieldRule.isValidRemark("a".repeat(513))).isFalse();
    }

    @Test
    @DisplayName("交易对象名称必须非空白且不超过64个字符")
    void counterpartyName_requiresNonBlankAndLimitedLength() {
        assertThat(FinanceOrderFieldRule.isValidCounterpartyName("张三")).isTrue();
        assertThat(FinanceOrderFieldRule.isValidCounterpartyName("a".repeat(64))).isTrue();
        assertThat(FinanceOrderFieldRule.isValidCounterpartyName("")).isFalse();
        assertThat(FinanceOrderFieldRule.isValidCounterpartyName("  ")).isFalse();
        assertThat(FinanceOrderFieldRule.isValidCounterpartyName("a".repeat(65))).isFalse();
    }
}
