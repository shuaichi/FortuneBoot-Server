package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 理财收益统计
 *
 * @author zhangchi118
 * @date 2026/8/4 19:41
 **/
@Data
public class FinanceProfitVo {

    private Long orderId;

    private String title;

    private BigDecimal outAmount;

    private BigDecimal inAmount;

    private BigDecimal profit;

    private BigDecimal profitRate;
}
