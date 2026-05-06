package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/8/25 21:38
 **/
@Slf4j
@Component
public class TransferBillStrategy extends AbstractBillStrategy {

    private final ApplicationScopeBo applicationScopeBo;

    public TransferBillStrategy(ApplicationScopeBo applicationScopeBo) {
        super(applicationScopeBo);
        this.applicationScopeBo = applicationScopeBo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBalance(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        if (Objects.isNull(billModel.getAccountId()) || Objects.isNull(billModel.getToAccountId())) {
            return;
        }
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneAccountModel toAccount = context.getToAccount();

        fromAccount.checkEnable();
        fromAccount.checkCanTransferOut();
        toAccount.checkEnable();
        toAccount.checkCanTransferIn();

        // 转出账户扣款 = 原转出金额 + from侧手续费 - from侧优惠
        BigDecimal fromDeduct = this.calcFromDeductAmount(context);
        fromAccount.addBalanceAtomic(fromDeduct.negate());

        // 转入账户入账 = 原转入金额(convertedAmount) - to侧手续费 + to侧优惠
        BigDecimal toCredit = this.calcToCreditAmount(context);
        toAccount.addBalanceAtomic(toCredit);
    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneAccountModel toAccount = context.getToAccount();

        // 回滚必须与 confirmBalance 保持对称
        BigDecimal fromDeduct = this.calcFromDeductAmount(context);
        fromAccount.addBalanceAtomic(fromDeduct);

        BigDecimal toCredit = this.calcToCreditAmount(context);
        toAccount.addBalanceAtomic(toCredit.negate());
    }

    /**
     * 转出账户实际扣款 = 转出金额 + from侧手续费 - from侧优惠
     */
    private BigDecimal calcFromDeductAmount(BillStrategyContext context) {
        BigDecimal amount = Objects.requireNonNullElse(context.getBillModel().getAmount(), BigDecimal.ZERO);
        BigDecimal fee = Objects.requireNonNullElse(context.getFromTotalFee(), BigDecimal.ZERO);
        BigDecimal discount = Objects.requireNonNullElse(context.getFromTotalDiscount(), BigDecimal.ZERO);
        return amount.add(fee).subtract(discount);
    }

    /**
     * 转入账户实际入账 = 转入金额(convertedAmount) - to侧手续费 + to侧优惠
     */
    private BigDecimal calcToCreditAmount(BillStrategyContext context) {
        BigDecimal convertedAmount = Objects.requireNonNullElse(context.getBillModel().getConvertedAmount(), BigDecimal.ZERO);
        BigDecimal fee = Objects.requireNonNullElse(context.getToTotalFee(), BigDecimal.ZERO);
        BigDecimal discount = Objects.requireNonNullElse(context.getToTotalDiscount(), BigDecimal.ZERO);
        return convertedAmount.subtract(fee).add(discount);
    }

    @Override
    public void convertRate(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneAccountModel toAccount = context.getToAccount();

        BigDecimal convertedAmount = Objects.nonNull(billModel.getConvertedAmount())
                ? billModel.getConvertedAmount()
                : super.convertCurrency(billModel.getAmount(), fromAccount.getCurrencyCode(), toAccount.getCurrencyCode(), applicationScopeBo.getCurrencyTemplateBoList());

        billModel.setConvertedAmount(convertedAmount);
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.TRANSFER;
    }
}