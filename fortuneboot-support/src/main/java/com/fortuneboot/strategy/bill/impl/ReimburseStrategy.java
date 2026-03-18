package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.factory.fortune.factory.FortuneFinanceOrderFactory;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.factory.fortune.model.FortuneFinanceOrderModel;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 报销策略
 *
 * @author zhangchi118
 * @date 2025/8/25 21:39
 **/
@Slf4j
@Component
public class ReimburseStrategy extends AbstractBillStrategy {

    private final FortuneFinanceOrderFactory fortuneFinanceOrderFactory;

    public ReimburseStrategy(ApplicationScopeBo applicationScopeBo, FortuneFinanceOrderFactory fortuneFinanceOrderFactory) {
        super(applicationScopeBo);
        this.fortuneFinanceOrderFactory = fortuneFinanceOrderFactory;
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        if (!billModel.getConfirm() || Objects.isNull(billModel.getAccountId())) {
            return;
        }

        // 报销账单：增加目标账户余额
        FortuneAccountModel fromAccount = context.getFromAccount();
        fromAccount.checkEnable();
        fromAccount.checkCanIncome();

        BigDecimal amount = context.getBillModel().getAmount();
        // 使用原子更新增加余额
        fromAccount.addBalanceAtomic(amount);
    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneBillModel billModel = context.getBillModel();

        // 使用原子更新退回余额 (使用 negate 转为负数进行扣除)
        fromAccount.addBalanceAtomic(billModel.getAmount().negate());
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.REIMBURSE;
    }

    @Override
    public void operateFinanceOrder(BillStrategyContext context) {
        FortuneBillAddCommand command = context.getCommand();
        FortuneBillModel billModel = context.getBillModel();
        billModel.checkOrderId();
        FortuneFinanceOrderModel orderModel = fortuneFinanceOrderFactory.loadById(command.getOrderId());
        orderModel.setInAmount(orderModel.getInAmount().add(billModel.getAmount()));
        orderModel.updateById();
    }

    @Override
    public void refuseFinanceOrder(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        if (Objects.isNull(billModel.getOrderId())) {
            return;
        }
        FortuneFinanceOrderModel orderModel = fortuneFinanceOrderFactory.loadById(billModel.getOrderId());
        orderModel.setInAmount(orderModel.getInAmount().subtract(billModel.getAmount()));
        orderModel.updateById();
    }
}