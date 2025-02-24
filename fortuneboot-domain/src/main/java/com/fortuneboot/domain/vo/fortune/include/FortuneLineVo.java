package com.fortuneboot.domain.vo.fortune.include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 折线图
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/2/23 15:48
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FortuneLineVo {

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private BigDecimal value;
}
