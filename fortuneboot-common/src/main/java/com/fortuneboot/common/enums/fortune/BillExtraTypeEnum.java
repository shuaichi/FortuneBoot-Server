package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 账单附加费用类型枚举
 * <p>
 * Excel 导入导出的中文名称属于稳定契约，序列化与解析必须统一走本枚举，
 * 禁止在导入、导出两侧分别手写编码或文案。
 *
 * @author fortuneboot
 */
@Getter
public enum BillExtraTypeEnum implements BasicEnum<Integer> {
    FEE(1, "手续费"),
    DISCOUNT(2, "优惠"),
    ;

    private final Integer value;

    private final String description;

    BillExtraTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BillExtraTypeEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }

    public static BillExtraTypeEnum getByDescription(String description) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.description, description))
                .findFirst()
                .orElse(null);
    }
}
