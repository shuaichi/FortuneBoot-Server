package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收支日历查询
 *
 * @author zhangchi118
 * @date 2026/8/13
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class IncomeExpenseCalendarQuery extends BillIncludeQuery {

    /**
     * 日历粒度：1-按日，2-按月，3-按年
     */
    @NotNull(message = "日历粒度不能为空")
    private Integer granularity;

    /**
     * 年份，按日和按月时必填
     */
    @Min(value = 1900, message = "年份不能早于1900")
    @Max(value = 2100, message = "年份不能晚于2100")
    private Integer year;

    /**
     * 月份，按日时必填
     */
    @Min(value = 1, message = "月份不能小于1")
    @Max(value = 12, message = "月份不能大于12")
    private Integer month;

    /**
     * 起始年份，按年时必填
     */
    @Min(value = 1900, message = "起始年份不能早于1900")
    @Max(value = 2100, message = "起始年份不能晚于2100")
    private Integer startYear;

    /**
     * 结束年份，按年时必填
     */
    @Min(value = 1900, message = "结束年份不能早于1900")
    @Max(value = 2100, message = "结束年份不能晚于2100")
    private Integer endYear;
}
