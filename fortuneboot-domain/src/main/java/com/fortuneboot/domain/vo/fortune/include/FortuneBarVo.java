package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 柱状图
 *
 * @author zhangchi118
 * @ate 2025/3/2 16:14
 **/
@Data
public class FortuneBarVo {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private BigDecimal value;
}
