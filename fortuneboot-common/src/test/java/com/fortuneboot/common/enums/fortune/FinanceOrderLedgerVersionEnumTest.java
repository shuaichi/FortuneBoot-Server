package com.fortuneboot.common.enums.fortune;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum.CURRENT;
import static com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum.LEGACY;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据账本版本领域语义")
class FinanceOrderLedgerVersionEnumTest {

    @Test
    @DisplayName("历史版本为0，当前版本为1")
    void legacyAndCurrentVersionsAreExplicit() {
        assertThat(LEGACY.getValue()).isZero();
        assertThat(CURRENT.getValue()).isOne();
        assertThat(FinanceOrderLedgerVersionEnum.getByValue(0)).isEqualTo(LEGACY);
        assertThat(FinanceOrderLedgerVersionEnum.getByValue(1)).isEqualTo(CURRENT);
        assertThat(FinanceOrderLedgerVersionEnum.getByValue(2)).isNull();
        assertThat(FinanceOrderLedgerVersionEnum.isCurrentValue(1)).isTrue();
        assertThat(FinanceOrderLedgerVersionEnum.isLegacyValue(null)).isFalse();
    }
}
