package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 收支日历粒度
 *
 * @author zhangchi118
 * @date 2026/8/13
 **/
@Getter
public enum IncomeExpenseCalendarGranularityEnum implements BasicEnum<Integer> {
    DAY(1, "按日"),
    MONTH(2, "按月"),
    YEAR(3, "按年"),
    ;

    private final Integer value;

    private final String description;

    IncomeExpenseCalendarGranularityEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static IncomeExpenseCalendarGranularityEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(item -> Objects.equals(item.value, value))
                .findFirst()
                .orElse(null);
    }
}
