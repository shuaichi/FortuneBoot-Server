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
        if ( Objects.isNull(billModel.getAccountId())|| Objects.isNull(billModel.getToAccountId())) {
            // TODO 报错
//            throw new ApiException();
            return;
        }
        FortuneAccountModel fromAccount = context.getFromAccount();
        FortuneAccountModel toAccount = context.getToAccount();
        BigDecimal amount = context.getBillModel().getAmount();

        // 转出账户：减少目标账户余额
        fromAccount.checkEnable();
        fromAccount.checkCanTransferOut();
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));

        // 转入账户：增加目标账户余额
        toAccount.checkEnable();
        toAccount.checkCanTransferIn();

        toAccount.setBalance(toAccount.getBalance().add(billModel.getAmount()));

        // 更新账户余额
        fromAccount.updateById();
        toAccount.updateById();
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
    public void refuseBalance(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();
        // 验证转账必要参数
        if (Objects.isNull(billModel.getToAccountId()) || Objects.isNull(billModel.getConvertedAmount())) {
            throw new ApiException(ErrorCode.Business.BILL_TRANSFER_PARAMETER_ERROR);
        }

        FortuneAccountModel fromAccount = context.getFromAccount();
        BigDecimal fromBalance = fromAccount.getBalance().add(billModel.getAmount());
        fromAccount.setBalance(fromBalance);

        FortuneAccountModel toAccount = context.getToAccount();
        BigDecimal toBalance = toAccount.getBalance().subtract(billModel.getAmount());
        toAccount.setBalance(toBalance);

        fromAccount.updateById();
        toAccount.updateById();
    }

    @Override
    public BillTypeEnum getSupportedBillType() {
        return BillTypeEnum.TRANSFER;
    }
}
