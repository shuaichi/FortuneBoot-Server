package com.fortuneboot.common.enums.fortune;

import lombok.Getter;

/**
 * 分类枚举
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:19
 **/
@Getter
public enum CategoryTypeEnum {

    EXPENSE(1, "支出分类"),
    INCOME(2, "收入分类");

    private final Integer value;

    private final String description;

    CategoryTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }


}
