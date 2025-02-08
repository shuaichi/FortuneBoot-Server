package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 分类枚举
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:19
 **/
@Getter
public enum CategoryTypeEnum implements BasicEnum<Integer> {

    EXPENSE(1, "支出分类"),
    INCOME(2, "收入分类");

    private final Integer value;

    private final String description;

    CategoryTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CategoryTypeEnum getByValue(Integer value){
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }
}
