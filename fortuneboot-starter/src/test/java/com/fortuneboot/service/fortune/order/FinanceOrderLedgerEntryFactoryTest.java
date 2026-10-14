package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据账本条目工厂")
class FinanceOrderLedgerEntryFactoryTest {

    private final FinanceOrderLedgerEntryFactory factory = new FinanceOrderLedgerEntryFactory();

    @Test
    @DisplayName("普通转换保留组件与版本，null版本按历史版本处理")
    void from_preservesVersionAndDefaultsNullToLegacy() {
        FinanceOrderLedgerEntry current = factory.from(bill(1, FinanceOrderComponentEnum.PRINCIPAL));
        assertThat(current.ledgerVersion()).isEqualTo(1);
        assertThat(current.component()).isEqualTo("PRINCIPAL");

        FinanceOrderLedgerEntry legacy = factory.from(bill(null, null));
        assertThat(legacy.ledgerVersion()).isZero();
    }

    @Test
    @DisplayName("布尔状态统一为原始boolean，null按false处理")
    void from_normalizesBooleanState() {
        FortuneBillEntity bill = bill(1, FinanceOrderComponentEnum.PRINCIPAL);
        bill.setConfirm(null);
        bill.setDeleted(null);
        bill.setRecycleBin(null);

        FinanceOrderLedgerEntry entry = factory.from(bill);

        assertThat(entry.confirmed()).isFalse();
        assertThat(entry.deleted()).isFalse();
        assertThat(entry.voided()).isFalse();
    }

    @Test
    @DisplayName("历史核对场景强制按当前账本版本解释流水")
    void fromCurrent_forcesCurrentLedgerVersion() {
        FinanceOrderLedgerEntry entry = factory.fromCurrent(bill(null, null));

        assertThat(entry.ledgerVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("历史报销单流水转换为PRINCIPAL组件和当前版本")
    void fromLegacyClaim_convertsToCurrentPrincipal() {
        FinanceOrderLedgerEntry entry = factory.fromLegacyClaim(bill(null, null));

        assertThat(entry.component()).isEqualTo(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        assertThat(entry.ledgerVersion()).isEqualTo(1);
    }

    private FortuneBillEntity bill(Integer ledgerVersion, FinanceOrderComponentEnum component) {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBillId(1L);
        bill.setBillType(7);
        bill.setOrderComponent(component == null ? null : component.getValue());
        bill.setAmount(new BigDecimal("100.00"));
        bill.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        bill.setConfirm(Boolean.TRUE);
        bill.setDeleted(false);
        bill.setRecycleBin(false);
        bill.setOrderLedgerVersion(ledgerVersion);
        return bill;
    }
}
