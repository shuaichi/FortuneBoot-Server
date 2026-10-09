package com.fortuneboot.common.enums.fortune;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum.CLOSE;
import static com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum.INIT;
import static com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum.USING;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据状态领域语义")
class FinanceOrderStatusEnumTest {

    @Test
    @DisplayName("根据编码查询枚举且未知编码返回空")
    void getByValueReturnsNullForUnknownValue() {
        assertThat(FinanceOrderStatusEnum.getByValue(INIT.getValue())).isEqualTo(INIT);
        assertThat(FinanceOrderStatusEnum.getByValue(null)).isNull();
        assertThat(FinanceOrderStatusEnum.getByValue(999)).isNull();
    }

    @Test
    @DisplayName("草稿和进行中可编辑，归档不可编辑")
    void editableStatusesAreInitialAndUsing() {
        assertThat(INIT.isInitial()).isTrue();
        assertThat(USING.isUsing()).isTrue();
        assertThat(CLOSE.isClosed()).isTrue();
        assertThat(INIT.isEditable()).isTrue();
        assertThat(USING.isEditable()).isTrue();
        assertThat(CLOSE.isEditable()).isFalse();
        assertThat(FinanceOrderStatusEnum.isEditableValue(CLOSE.getValue())).isFalse();
        assertThat(FinanceOrderStatusEnum.isEditableValue(null)).isFalse();
    }
}
