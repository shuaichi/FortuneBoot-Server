package com.fortuneboot.strategy.bill;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;

/**
 * 抽象的账单策略
 *
 * @author zhangchi118
 * @date 2025/8/25 21:23
 **/
public interface BillProcessStrategy {

    /**
     * 余额确认处理
     *
     * @param context
     */
    void confirmBalance(BillStrategyContext context);

    /**
     * 汇率转换
     *
     * @param context
     */
    void convertRate(BillStrategyContext context);

    /**
     * 金额回滚
     *
     * @param context
     */
    void refuseBalance(BillStrategyContext context);

    /**
     * 支持的账单类型
     *
     * @return
     */
    BillTypeEnum getSupportedBillType();
}
