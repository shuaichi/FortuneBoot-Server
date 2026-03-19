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
 *
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

        // 使用原子更新
        fromAccount.addBalanceAtomic(billModel.getAmount().negate());
        // 这里必须使用 convertedAmount，否则汇率不对
        toAccount.addBalanceAtomic(billModel.getConvertedAmount());
    }

    @Override
    public void refuseBalance(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneAccountModel toAccount = context.getToAccount();

        // 回滚必须使用原子操作，并区分原币种与目标币种金额
        fromAccount.addBalanceAtomic(billModel.getAmount());
        toAccount.addBalanceAtomic(billModel.getConvertedAmount().negate());
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
