package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 日历热力图
 *
 * @author zhangchi118
 * @date 2026/8/4 19:38
 **/
@Data
public class HeatmapVo {

    private String date;

    private BigDecimal amount;

    private Integer count;
}
