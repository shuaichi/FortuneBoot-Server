package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:38
 **/
@Component
public class LossBillStrategy extends AbstractBillStrategy {
    public LossBillStrategy(ApplicationScopeBo applicationScopeBo) {
        super(applicationScopeBo);
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {

    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneBillModel billModel = context.getBillModel();
        BigDecimal newBalance = fromAccount.getBalance().add(billModel.getAmount());
        fromAccount.setBalance(newBalance);
        fromAccount.updateById();
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.LOSS;
    }
}
