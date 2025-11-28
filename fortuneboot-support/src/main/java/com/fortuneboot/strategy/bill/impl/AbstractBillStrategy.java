package com.fortuneboot.strategy.bill.impl;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillProcessStrategy;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 22:00
 **/
abstract class AbstractBillStrategy implements BillProcessStrategy {

    @Resource
    private ApplicationScopeBo applicationScopeBo;

    /**
     * 货币转换 - 修复版本
     *
     * @param amount         原始金额
     * @param sourceCurrency 源币种
     * @param targetCurrency 目标币种
     * @param rateList       汇率列表 (格式: 1 USD = rate 本币)
     */
    protected BigDecimal convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency, List<CurrencyTemplateBo> rateList) {
        if (sourceCurrency.equals(targetCurrency)) {
            return amount;
        }

        // 获取源币种汇率 (1 USD = sourceRate 源币种)
        BigDecimal sourceRate = rateList.stream()
                .filter(rate -> rate.getCurrencyName().equals(sourceCurrency))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.APR_NOT_FOUND, sourceCurrency, "USD"));

        // 获取目标币种汇率 (1 USD = targetRate 目标币种)
        BigDecimal targetRate = rateList.stream()
                .filter(rate -> rate.getCurrencyName().equals(targetCurrency))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.APR_NOT_FOUND, "USD", targetCurrency));

        // 汇率有效性校验
        this.validateExchangeRate(sourceRate, sourceCurrency);
        this.validateExchangeRate(targetRate, targetCurrency);

        // 正确的转换公式：
        // 步骤1: 源币种 → USD: amount ÷ sourceRate
        // 步骤2: USD → 目标币种: usdAmount × targetRate
        BigDecimal usdAmount = amount.divide(sourceRate, 10, RoundingMode.HALF_UP);
        return usdAmount.multiply(targetRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 汇率校验
     */
    private void validateExchangeRate(BigDecimal rate, String currency) {
        if (Objects.isNull(rate) || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.Business.INVALID_EXCHANGE_RATE, currency, rate);
        }
    }

    @Override
    public void convertRate(BillStrategyContext context) {
        FortuneBillModel billModel = context.getBillModel();

        BigDecimal amount = context.getCommand().getCategoryAmountPair().stream()
                .map(CategoryAmountDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        billModel.setAmount(amount);

        // 对于非转账交易，需要根据账户币种和账本默认币种进行转换
        if (Objects.nonNull(billModel.getAccountId())) {
            FortuneAccountModel accountModel = context.getFromAccount();
            BigDecimal convertedAmount = this.convertCurrency(
                    amount,
                    accountModel.getCurrencyCode(),
                    context.getBookModel().getDefaultCurrency(),
                    applicationScopeBo.getCurrencyTemplateBoList()
            );
            billModel.setConvertedAmount(convertedAmount);
        } else {
            billModel.setConvertedAmount(amount);
        }

    }

    @Override
    public void operateFinanceOrder(BillStrategyContext context){}

    @Override
    public void refuseFinanceOrder(BillStrategyContext context){}
}
