package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 折线图
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/2/23 15:48
 **/
@Data
public class FortuneLineVo {

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * 名称
     */
    private String name;
}
