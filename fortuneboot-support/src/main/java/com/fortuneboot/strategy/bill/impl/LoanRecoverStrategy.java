package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.service.fortune.order.FinanceOrderCashEffectCalculator;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:38
 **/
@Component
public class LoanRecoverStrategy extends AbstractBillStrategy {
    private final FinanceOrderCashEffectCalculator cashEffectCalculator;

    public LoanRecoverStrategy(ApplicationScopeBo applicationScopeBo,
                               FinanceOrderCashEffectCalculator cashEffectCalculator) {
        super(applicationScopeBo);
        this.cashEffectCalculator = cashEffectCalculator;
    }

    @Override
    public void confirmBalance(BillStrategyContext context) {
        applyCashEffect(context, false);

    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        applyCashEffect(context, true);

    }

    private void applyCashEffect(BillStrategyContext context, boolean reverse) {
        cashEffectCalculator.calculate(context.getBillModel())
                .forEach((accountId, amount) -> context.getFromAccount().addBalanceAtomic(reverse ? amount.negate() : amount));
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.LOAN_RECOVER;
    }
}
