package com.fortuneboot.common.enums.common;

import lombok.Getter;

/**
 * 逻辑删除枚举
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 22:42
 **/
@Getter
public enum DeleteEnum {

    VALID(Boolean.FALSE, "有效"),
    INVALID(Boolean.TRUE, "无效"),
    ;
    private final Boolean value;

    private final String description;

    DeleteEnum(Boolean value, String description) {
        this.value = value;
        this.description = description;
    }
}
