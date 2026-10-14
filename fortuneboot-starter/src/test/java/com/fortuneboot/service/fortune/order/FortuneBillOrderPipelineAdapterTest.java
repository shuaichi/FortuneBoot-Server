package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("普通流水到单据管线适配规则")
class FortuneBillOrderPipelineAdapterTest {

    private final FortuneBillOrderPipelineAdapter adapter =
            new FortuneBillOrderPipelineAdapter(mock(FinanceOrderRequestService.class));

    @Test
    @DisplayName("关联单据或托管类型的流水必须进入单据管线")
    void orderEntry_requiresOrderIdOrManagedBillType() {
        FortuneBillAddCommand withOrder = command(BillTypeEnum.EXPENSE.getValue());
        withOrder.setOrderId(30L);
        assertThat(adapter.isOrderEntry(withOrder)).isTrue();

        FortuneBillAddCommand managedType = command(BillTypeEnum.LOAN_OUT.getValue());
        assertThat(adapter.isOrderEntry(managedType)).isTrue();

        FortuneBillAddCommand normalBill = command(BillTypeEnum.EXPENSE.getValue());
        assertThat(adapter.isOrderEntry(normalBill)).isFalse();
    }

    @Test
    @DisplayName("修改时新旧任一侧命中单据语义即进入单据管线")
    void orderEntry_checksBothOriginalBillAndCommand() {
        FortuneBillEntity legacyOrderBill = bill(30L, BillTypeEnum.EXPENSE.getValue(), null);
        FortuneBillAddCommand normalCommand = command(BillTypeEnum.EXPENSE.getValue());
        assertThat(adapter.isOrderEntry(legacyOrderBill, normalCommand)).isTrue();

        FortuneBillEntity normalBill = bill(null, BillTypeEnum.EXPENSE.getValue(),
                FinanceOrderLedgerVersionEnum.CURRENT.getValue());
        FortuneBillAddCommand managedCommand = command(BillTypeEnum.REIMBURSE.getValue());
        assertThat(adapter.isOrderEntry(normalBill, managedCommand)).isTrue();
        assertThat(adapter.isOrderEntry(normalBill, normalCommand)).isFalse();
    }

    @Test
    @DisplayName("仅当前账本版本的关联流水由单据管线托管")
    void managedOrderBill_requiresOrderIdAndCurrentLedgerVersion() {
        assertThat(adapter.isManagedOrderBill(bill(30L, BillTypeEnum.LOAN_OUT.getValue(),
                FinanceOrderLedgerVersionEnum.CURRENT.getValue()))).isTrue();
        assertThat(adapter.isManagedOrderBill(bill(30L, BillTypeEnum.LOAN_OUT.getValue(),
                FinanceOrderLedgerVersionEnum.LEGACY.getValue()))).isFalse();
        assertThat(adapter.isManagedOrderBill(bill(null, BillTypeEnum.LOAN_OUT.getValue(),
                FinanceOrderLedgerVersionEnum.CURRENT.getValue()))).isFalse();
    }

    private FortuneBillAddCommand command(Integer billType) {
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setBookId(9L);
        command.setBillType(billType);
        return command;
    }

    private FortuneBillEntity bill(Long orderId, Integer billType, Integer ledgerVersion) {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBookId(9L);
        bill.setOrderId(orderId);
        bill.setBillType(billType);
        bill.setOrderLedgerVersion(ledgerVersion);
        return bill;
    }
}
