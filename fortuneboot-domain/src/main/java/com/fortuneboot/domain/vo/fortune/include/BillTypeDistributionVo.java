package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单类型分布
 *
 * @author zhangchi118
 * @date 2026/8/4 19:46
 **/
@Data
public class BillTypeDistributionVo {

    private Integer billType;

    private String name;

    private BigDecimal value;

    private BigDecimal percent;
}
