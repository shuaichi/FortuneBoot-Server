package com.fortuneboot.common.enums.common;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

/**
 * @author zhangchi118
 * @date 2025/6/30 10:19
 **/
@Getter
public enum TrueFalseEnum implements BasicEnum<Integer> {
    TRUE(1,"true"),
    FALSE(0,"false"),
    ;

    private final Integer value;

    private final String description;

    TrueFalseEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
