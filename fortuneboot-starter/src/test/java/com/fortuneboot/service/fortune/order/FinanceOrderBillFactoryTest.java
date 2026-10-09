package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据流水实体工厂")
class FinanceOrderBillFactoryTest {

    private final FinanceOrderBillFactory factory = new FinanceOrderBillFactory();

    @Test
    @DisplayName("ADD构建完整映射命令字段并初始化当前版本与有效状态")
    void buildAddBill_mapsCommandAndInitializesState() {
        FinanceOrderEntryCommand command = entryCommand();

        FortuneBillEntity bill = factory.buildAddBill(command);

        assertThat(bill.getBillId()).isNull();
        assertThat(bill.getBookId()).isEqualTo(9L);
        assertThat(bill.getTitle()).isEqualTo("垫付");
        assertThat(bill.getTradeTime()).isEqualTo(LocalDateTime.of(2026, 10, 1, 9, 0));
        assertThat(bill.getAccountId()).isEqualTo(10L);
        assertThat(bill.getAmount()).isEqualByComparingTo("100.00");
        assertThat(bill.getConvertedAmount()).isEqualByComparingTo("100.00");
        assertThat(bill.getPayeeId()).isEqualTo(20L);
        assertThat(bill.getBillType()).isEqualTo(7);
        assertThat(bill.getToAccountId()).isNull();
        assertThat(bill.getConfirm()).isTrue();
        assertThat(bill.getInclude()).isTrue();
        assertThat(bill.getRemark()).isEqualTo("备注");
        assertThat(bill.getOrderId()).isEqualTo(30L);
        assertThat(bill.getOrderComponent()).isEqualTo("PRINCIPAL");
        assertThat(bill.getOrderLedgerVersion())
                .isEqualTo(FinanceOrderLedgerVersionEnum.CURRENT.getValue());
        assertThat(bill.getDeleted()).isFalse();
        assertThat(bill.getRecycleBin()).isFalse();
    }

    @Test
    @DisplayName("复制流水保留实体全部字段，包括主键与审计字段")
    void copyBill_copiesAllEntityFields() {
        FortuneBillEntity source = bill();
        source.setBillId(1L);
        source.setDeleted(true);
        source.setRecycleBin(true);
        source.setCreatorId(99L);
        source.setCreateTime(LocalDateTime.of(2026, 10, 1, 8, 0));
        source.setUpdaterId(98L);
        source.setUpdateTime(LocalDateTime.of(2026, 10, 1, 9, 0));

        FortuneBillEntity target = factory.copyBill(source);

        assertThat(target).usingRecursiveComparison().isEqualTo(source);
    }

    @Test
    @DisplayName("MODIFY只更新可编辑字段，不动账本版本、组件与类型")
    void applyModify_updatesOnlyEditableFields() {
        FortuneBillEntity bill = bill();
        bill.setBillId(1L);
        bill.setOrderLedgerVersion(FinanceOrderLedgerVersionEnum.CURRENT.getValue());

        FinanceOrderEntryCommand command = entryCommand();
        command.setTitle("修改后");
        command.setTradeTime(LocalDateTime.of(2026, 10, 3, 9, 0));
        command.setAccountId(11L);
        command.setAmount(new BigDecimal("88.00"));
        command.setConvertedAmount(new BigDecimal("88.00"));
        command.setPayeeId(21L);
        command.setConfirm(false);
        command.setInclude(false);
        command.setRemark("新备注");

        factory.applyModify(bill, command);

        assertThat(bill.getTitle()).isEqualTo("修改后");
        assertThat(bill.getTradeTime()).isEqualTo(LocalDateTime.of(2026, 10, 3, 9, 0));
        assertThat(bill.getAccountId()).isEqualTo(11L);
        assertThat(bill.getAmount()).isEqualByComparingTo("88.00");
        assertThat(bill.getConvertedAmount()).isEqualByComparingTo("88.00");
        assertThat(bill.getPayeeId()).isEqualTo(21L);
        assertThat(bill.getConfirm()).isFalse();
        assertThat(bill.getInclude()).isFalse();
        assertThat(bill.getRemark()).isEqualTo("新备注");
        assertThat(bill.getBillType()).isEqualTo(7);
        assertThat(bill.getOrderComponent()).isEqualTo("PRINCIPAL");
        assertThat(bill.getOrderLedgerVersion())
                .isEqualTo(FinanceOrderLedgerVersionEnum.CURRENT.getValue());
        assertThat(bill.getBookId()).isEqualTo(9L);
        assertThat(bill.getOrderId()).isEqualTo(30L);
    }

    private FinanceOrderEntryCommand entryCommand() {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(9L);
        command.setTitle("垫付");
        command.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        command.setAccountId(10L);
        command.setAmount(new BigDecimal("100.00"));
        command.setConvertedAmount(new BigDecimal("100.00"));
        command.setPayeeId(20L);
        command.setBillType(7);
        command.setConfirm(true);
        command.setInclude(true);
        command.setRemark("备注");
        command.setOrderId(30L);
        command.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        return command;
    }

    private FortuneBillEntity bill() {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBookId(9L);
        bill.setTitle("垫付");
        bill.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        bill.setAccountId(10L);
        bill.setAmount(new BigDecimal("100.00"));
        bill.setConvertedAmount(new BigDecimal("100.00"));
        bill.setPayeeId(20L);
        bill.setBillType(7);
        bill.setConfirm(true);
        bill.setInclude(true);
        bill.setRemark("备注");
        bill.setOrderId(30L);
        bill.setOrderComponent("PRINCIPAL");
        bill.setOrderLedgerVersion(FinanceOrderLedgerVersionEnum.CURRENT.getValue());
        return bill;
    }
}
