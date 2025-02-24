package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangchi118
 * @date 2025/2/22 20:59
 **/
@Data
public class BillStatisticsVo {

    /**
     * 收入
     */
    private BigDecimal income;

    /**
     * 支出
     */
    private BigDecimal expense;

    /**
     * 结余
     */
    private BigDecimal surplus;
}
