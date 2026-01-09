package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import lombok.AllArgsConstructor;
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
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));

        fromAccount.updateById();

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
        return BillTypeEnum.EXPENSE;
    }

}
