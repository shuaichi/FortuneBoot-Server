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

        // 实际扣款 = 原账单金额 + 手续费 - 优惠（均保证非空）
        BigDecimal actualAmount = this.calcActualExpenseAmount(context);
        // 使用原子更新扣除余额 (使用 negate 转为负数进行扣款)
        fromAccount.addBalanceAtomic(actualAmount.negate());
    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneAccountModel fromAccount = context.getFromAccount();

        // 回滚必须与 confirmBalance 保持对称：原金额 + 手续费 - 优惠
        BigDecimal actualAmount = this.calcActualExpenseAmount(context);
        // 使用原子更新退回余额 (正数增加)
        fromAccount.addBalanceAtomic(actualAmount);
    }

    /**
     * 计算支出场景实际扣款金额 = 账单金额 + 手续费 - 优惠
     * 注意：保持不可变性，返回新的 BigDecimal 实例而非修改 billModel.amount
     */
    private BigDecimal calcActualExpenseAmount(BillStrategyContext context) {
        BigDecimal amount = Objects.requireNonNullElse(context.getBillModel().getAmount(), BigDecimal.ZERO);
        BigDecimal fee = Objects.requireNonNullElse(context.getTotalFee(), BigDecimal.ZERO);
        BigDecimal discount = Objects.requireNonNullElse(context.getTotalDiscount(), BigDecimal.ZERO);
        return amount.add(fee).subtract(discount);
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.EXPENSE;
    }

}