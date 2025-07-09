package com.fortuneboot.domain.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端Select选择器Vo
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 01:09
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectOptionsVo {

    /**
     * value
     */
    private Long value;

    /**
     * label
     */
    private String label;
}
