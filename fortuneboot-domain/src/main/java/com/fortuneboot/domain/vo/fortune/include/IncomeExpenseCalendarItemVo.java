package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收支日历时间桶
 *
 * @author zhangchi118
 * @date 2026/8/13
 **/
@Data
public class IncomeExpenseCalendarItemVo {

    private String period;

    private BigDecimal income;

    private BigDecimal expense;

    private Integer incomeCount;

    private Integer expenseCount;
}
