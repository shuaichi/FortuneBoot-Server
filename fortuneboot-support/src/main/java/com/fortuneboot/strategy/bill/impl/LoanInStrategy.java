package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:39
 **/
@Component
public class LoanInStrategy extends AbstractBillStrategy {

    public LoanInStrategy(ApplicationScopeBo applicationScopeBo) {
        super(applicationScopeBo);
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {

    }

    @Override
    public void refuseBalance(BillStrategyContext context) {

    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.LOAN_IN;
    }
}
