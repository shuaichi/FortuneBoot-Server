package com.fortuneboot.domain.vo.fortune;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 归物统计Vo
 *
 * @author zhangchi118
 * @date 2025/5/10 15:54
 **/
@Data
public class FortuneGoodsKeeperStatisticsVo {

    /**
     * 全部物品的成本
     */
    private BigDecimal allPrice;

    /**
     * 日均成本
     */
    private BigDecimal allDailyPrice;

    /**
     * 在役物品成本
     */
    private BigDecimal activePrice;

    /**
     * 在役物品日均成本
     */
    private BigDecimal activeDailyPrice;
}
