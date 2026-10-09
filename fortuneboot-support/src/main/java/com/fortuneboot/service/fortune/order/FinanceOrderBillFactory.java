package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.springframework.stereotype.Component;

/**
 * 单据流水实体工厂。
 * <p>用显式字段映射替代反射复制，统一ADD/MODIFY的状态初始化，避免隐式字段漂移。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
public class FinanceOrderBillFactory {

    /**
     * 构建新增流水；主键留空由数据库生成，默认当前账本版本且有效。
     */
    public FortuneBillEntity buildAddBill(FinanceOrderEntryCommand command) {
        FortuneBillEntity bill = new FortuneBillEntity();
        applyEditableFields(bill, command);
        bill.setBookId(command.getBookId());
        bill.setBillType(command.getBillType());
        bill.setOrderId(command.getOrderId());
        bill.setOrderComponent(command.getOrderComponent());
        bill.setOrderLedgerVersion(FinanceOrderLedgerVersionEnum.CURRENT.getValue());
        bill.setDeleted(false);
        bill.setRecycleBin(false);
        return bill;
    }

    /**
     * 复制流水全部实体字段，包括主键与审计字段，用于状态动作和修改前快照。
     */
    public FortuneBillEntity copyBill(FortuneBillEntity source) {
        FortuneBillEntity target = new FortuneBillEntity();
        target.setBillId(source.getBillId());
        target.setBookId(source.getBookId());
        target.setTitle(source.getTitle());
        target.setTradeTime(source.getTradeTime());
        target.setAccountId(source.getAccountId());
        target.setAmount(source.getAmount());
        target.setConvertedAmount(source.getConvertedAmount());
        target.setPayeeId(source.getPayeeId());
        target.setBillType(source.getBillType());
        target.setToAccountId(source.getToAccountId());
        target.setConfirm(source.getConfirm());
        target.setInclude(source.getInclude());
        target.setOrderId(source.getOrderId());
        target.setOrderComponent(source.getOrderComponent());
        target.setOrderLedgerVersion(source.getOrderLedgerVersion());
        target.setRemark(source.getRemark());
        target.setRecycleBin(source.getRecycleBin());
        target.setCreatorId(source.getCreatorId());
        target.setCreateTime(source.getCreateTime());
        target.setUpdaterId(source.getUpdaterId());
        target.setUpdateTime(source.getUpdateTime());
        target.setDeleted(source.getDeleted());
        return target;
    }

    /**
     * 修改流水可编辑字段；账本版本、组件、类型和归属不允许变化。
     */
    public void applyModify(FortuneBillEntity bill, FinanceOrderEntryCommand command) {
        applyEditableFields(bill, command);
    }

    private void applyEditableFields(FortuneBillEntity bill, FinanceOrderEntryCommand command) {
        bill.setTitle(command.getTitle());
        bill.setTradeTime(command.getTradeTime());
        bill.setAccountId(command.getAccountId());
        bill.setAmount(command.getAmount());
        bill.setConvertedAmount(command.getConvertedAmount());
        bill.setPayeeId(command.getPayeeId());
        bill.setConfirm(command.getConfirm());
        bill.setInclude(command.getInclude());
        bill.setRemark(command.getRemark());
        bill.setToAccountId(command.getToAccountId());
    }
}
