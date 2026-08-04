package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.time.LocalDate;

/**
 * 理财收益统计查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:45
 **/
@Data
public class FinanceProfitQuery {

    private LocalDate startDate;

    private LocalDate endDate;
}
