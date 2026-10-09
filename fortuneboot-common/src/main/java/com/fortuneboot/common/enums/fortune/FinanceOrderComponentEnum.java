package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 单据流水组件枚举
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Getter
public enum FinanceOrderComponentEnum implements BasicEnum<String> {

    PRINCIPAL("PRINCIPAL", "本金"),
    INTEREST("INTEREST", "利息"),
    FEE("FEE", "手续费"),
    WRITE_OFF("WRITE_OFF", "差额结清"),
    ;

    private final String value;

    private final String description;

    FinanceOrderComponentEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FinanceOrderComponentEnum getByValue(String value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }
}
