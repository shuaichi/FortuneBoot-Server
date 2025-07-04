package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

/**
 * @author zhangchi118
 * @date 2025/7/4 20:32
 **/
@Getter
public enum RecurringBillLogStatusEnum implements BasicEnum<Integer> {
    RUNNING(1, "执行中"),
    SUCCESS(2, "成功"),
    FAILURE(3, "失败"),
    ;

    private final Integer value;
    private final String description;

    RecurringBillLogStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
