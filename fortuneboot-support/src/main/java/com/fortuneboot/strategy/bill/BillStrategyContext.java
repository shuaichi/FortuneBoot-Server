package com.fortuneboot.strategy.bill;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单策略上下文
 * @author zhangchi118
 * @date 2025/8/25 21:26
 **/
@Data
public class BillStrategyContext {

    private FortuneBillAddCommand command;

    /**
     * 账单
     */
    private FortuneBillModel billModel;

    /**
     * 账本
     */
    private FortuneBookModel bookModel;

    /**
     * 原账户
     */
    private FortuneAccountModel fromAccount;

    /**
     * 目的账户
     */
    private FortuneAccountModel toAccount;

    /**
     * 金额
     */
    private BigDecimal amount;


}
