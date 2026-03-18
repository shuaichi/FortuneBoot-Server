package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:38
 **/
@Component
public class IncomeBillStrategy extends AbstractBillStrategy {

    public IncomeBillStrategy(ApplicationScopeBo applicationScopeBo) {
        super(applicationScopeBo);
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        if (!billModel.getConfirm() || Objects.isNull(billModel.getAccountId())) {
            return;
        }

        // 收入账单：增加目标账户余额
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
        return BillTypeEnum.INCOME;
    }

}