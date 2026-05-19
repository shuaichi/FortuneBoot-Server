package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:38
 **/
@Slf4j
@Component
public class AdjustBillStrategy extends AbstractBillStrategy{

    public AdjustBillStrategy(ApplicationScopeBo applicationScopeBo) {
        super(applicationScopeBo);
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {

    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneBillModel billModel = context.getBillModel();
        BigDecimal newBalance = fromAccount.getBalance().subtract(billModel.getAmount());
        fromAccount.setBalance(newBalance);
        fromAccount.updateById();
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.ADJUST;
    }

    @Override
    public void convertRate(BillStrategyContext context){
        // 余额调整不需要汇率转换，但需要从 command 中获取金额设置到 billModel
        // 因为 loadAddCommand 排除了 amount 和 convertedAmount，所以这里必须手动设置
        FortuneBillModel billModel = context.getBillModel();
        BigDecimal amount = Objects.requireNonNullElse(context.getCommand().getAmount(), BigDecimal.ZERO);
        billModel.setAmount(amount);
        billModel.setConvertedAmount(amount);
    }
}
