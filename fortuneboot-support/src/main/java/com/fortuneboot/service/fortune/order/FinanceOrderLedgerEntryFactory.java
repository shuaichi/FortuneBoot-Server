package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.springframework.stereotype.Component;

/**
 * 单据账本条目工厂。
 * <p>统一实体到纯计算入参的转换，消除查询、写入、统计和历史核对中的重复映射。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
public class FinanceOrderLedgerEntryFactory {

    /**
     * 按流水实际账本版本转换；历史缺失版本按LEGACY解释。
     */
    public FinanceOrderLedgerEntry from(FortuneBillEntity bill) {
        return new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(), bill.getOrderComponent(),
                bill.getAmount(), bill.getTradeTime(), Boolean.TRUE.equals(bill.getConfirm()),
                Boolean.TRUE.equals(bill.getDeleted()), Boolean.TRUE.equals(bill.getRecycleBin()),
                bill.getOrderLedgerVersion() == null
                        ? FinanceOrderLedgerVersionEnum.LEGACY.getValue() : bill.getOrderLedgerVersion());
    }

    /**
     * 历史核对场景：候选解释强制使用当前账本版本。
     */
    public FinanceOrderLedgerEntry fromCurrent(FortuneBillEntity bill) {
        FinanceOrderLedgerEntry entry = from(bill);
        return new FinanceOrderLedgerEntry(entry.billId(), entry.billType(), entry.component(),
                entry.amount(), entry.tradeTime(), entry.confirmed(), entry.deleted(), entry.voided(),
                FinanceOrderLedgerVersionEnum.CURRENT.getValue());
    }

    /**
     * 历史报销单流水统一解释为PRINCIPAL组件和当前账本版本。
     */
    public FinanceOrderLedgerEntry fromLegacyClaim(FortuneBillEntity bill) {
        FinanceOrderLedgerEntry entry = from(bill);
        return new FinanceOrderLedgerEntry(entry.billId(), entry.billType(),
                FinanceOrderComponentEnum.PRINCIPAL.getValue(), entry.amount(), entry.tradeTime(),
                entry.confirmed(), entry.deleted(), entry.voided(),
                FinanceOrderLedgerVersionEnum.CURRENT.getValue());
    }
}
