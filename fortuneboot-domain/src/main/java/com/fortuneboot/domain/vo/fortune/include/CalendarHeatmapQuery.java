package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日历热力图查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:38
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarHeatmapQuery extends BillIncludeQuery {

    /**
     * 年份
     */
    @NotNull(message = "年份不能为空")
    @Min(value = 1900, message = "年份不能早于1900")
    @Max(value = 2100, message = "年份不能晚于2100")
    private Integer year;
}
