package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收支对比
 *
 * @author zhangchi118
 * @date 2026/8/4 19:36
 **/
@Data
public class BillCompareVo {

    private String name;

    private BigDecimal income;

    private BigDecimal expense;
}
