package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 收支日历
 *
 * @author zhangchi118
 * @date 2026/8/13
 **/
@Data
public class IncomeExpenseCalendarVo {

    private Integer granularity;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<IncomeExpenseCalendarItemVo> items;
}
