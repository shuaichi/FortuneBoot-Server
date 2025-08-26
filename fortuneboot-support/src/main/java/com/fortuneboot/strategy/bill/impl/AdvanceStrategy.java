package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:39
 **/
@Component
public class AdvanceStrategy extends AbstractBillStrategy{

    @Override
    public void confirmBalance(BillStrategyContext context) {

    }

    @Override
    public void refuseBalance(BillStrategyContext context) {

    }

    @Override
    public void validateBusiness(BillStrategyContext context) {

    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.ADVANCE;
    }
}
