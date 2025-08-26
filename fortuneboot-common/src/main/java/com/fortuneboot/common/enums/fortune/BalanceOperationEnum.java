package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

/**
 * 余额操作枚举
 *
 * @author zhangchi118
 * @date 2025/2/6 19:43
 **/
@Getter
public enum BalanceOperationEnum implements BasicEnum<Integer> {
    INCREASE(1, "余额增加"),
    DECREASE(2, "余额减少"),
    ;


    private final Integer value;

    private final String description;

    BalanceOperationEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
