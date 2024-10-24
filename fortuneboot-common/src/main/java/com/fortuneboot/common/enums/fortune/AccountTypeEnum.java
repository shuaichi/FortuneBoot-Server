package com.fortuneboot.common.enums.fortune;

import lombok.Getter;

/**
 * 账户类型枚举
 *
 * @author work.chi.zhang@gmail.com
 * @date 2024年06月03日 22:24:17
 */
@Getter
public enum AccountTypeEnum {
    CURRENT(1, "活期"),
    CREDIT(2, "信用"),
    ASSET(3, "资产"),
    LOAN(4, "贷款"),
    ;
    private final Integer value;

    private final String description;

    AccountTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
