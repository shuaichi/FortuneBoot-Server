package com.fortuneboot.common.enums.fortune;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 单据关联流水账本版本枚举。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Getter
public enum FinanceOrderLedgerVersionEnum {

    /**
     * 历史版本：无组件字段，由流水类型推断。
     */
    LEGACY(0),

    /**
     * 当前版本：显式记录组件与版本。
     */
    CURRENT(1),
    ;

    private final Integer value;

    FinanceOrderLedgerVersionEnum(Integer value) {
        this.value = value;
    }

    public static FinanceOrderLedgerVersionEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(version -> Objects.equals(version.getValue(), value))
                .findFirst()
                .orElse(null);
    }

    public static boolean isCurrentValue(Integer value) {
        return CURRENT.getValue().equals(value);
    }

    public static boolean isLegacyValue(Integer value) {
        return LEGACY.getValue().equals(value);
    }
}
