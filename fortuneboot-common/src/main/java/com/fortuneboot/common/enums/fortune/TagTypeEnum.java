package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import com.fortuneboot.common.enums.dictionary.Dictionary;
import lombok.Getter;

/**
 * 标签类型枚举
 *
 * @author zhangchi118
 * @date 2024/12/11 17:24
 **/
@Getter
@Dictionary(name = "fortune.tagType")
public enum TagTypeEnum implements BasicEnum<Integer> {

    EXPENSE(1, "支出标签"),
    INCOME(2, "收入标签"),
    ;

    private final Integer value;

    private final String description;

    TagTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
