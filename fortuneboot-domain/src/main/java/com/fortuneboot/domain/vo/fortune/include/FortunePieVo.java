package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangchi118
 * @date 2025/2/22 22:14
 **/
@Data
public class FortunePieVo {

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * 名称
     */
    private String name;

    /**
     * 百分比
     */
    private BigDecimal percent;
}
