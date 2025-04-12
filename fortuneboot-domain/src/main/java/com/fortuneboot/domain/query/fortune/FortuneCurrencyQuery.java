package com.fortuneboot.domain.query.fortune;

import lombok.Data;

/**
 * 汇率查询
 *
 * @author zhangchi118
 * @date 2025/4/4 21:29
 **/
@Data
public class FortuneCurrencyQuery {

    /**
     * 目标
     */
    private String target;

    /**
     * 基准
     */
    private String base;

}
