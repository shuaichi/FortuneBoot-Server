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
public class ExpenseBillStrategy extends AbstractBillStrategy {

    public ExpenseBillStrategy(ApplicationScopeBo applicationScopeBo) {
        super(applicationScopeBo);
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {
        // 支出账单：减少源账户余额
        FortuneBillModel billModel = context.getBillModel();
        if (!billModel.getConfirm() || Objects.isNull(billModel.getAccountId())) {
            return;
        }

        FortuneAccountModel fromAccount = context.getFromAccount();
        fromAccount.checkEnable();
        fromAccount.checkCanExpense();

        BigDecimal amount = context.getBillModel().getAmount();
        // 使用原子更新扣除余额 (使用 negate 转为负数进行扣款)
        fromAccount.addBalanceAtomic(amount.negate());
    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneBillModel billModel = context.getBillModel();

        // 使用原子更新退回余额 (正数增加)
        fromAccount.addBalanceAtomic(billModel.getAmount());
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.EXPENSE;
    }

}