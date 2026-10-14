package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 账单附加费用记账账户方向枚举
 * <p>
 * Excel 导入导出的中文名称属于稳定契约，序列化与解析必须统一走本枚举。
 *
 * @author fortuneboot
 */
@Getter
public enum BillExtraAccountSideEnum implements BasicEnum<Integer> {
    FROM(1, "转出账户"),
    TO(2, "转入账户"),
    ;

    private final Integer value;

    private final String description;

    BillExtraAccountSideEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BillExtraAccountSideEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }

    public static BillExtraAccountSideEnum getByDescription(String description) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.description, description))
                .findFirst()
                .orElse(null);
    }
}
