package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 单据历史核对状态枚举
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Getter
public enum FinanceOrderReconciliationStatusEnum implements BasicEnum<String> {

    READY("READY", "已核对"),
    REVIEW_REQUIRED("REVIEW_REQUIRED", "待核对"),
    ;

    private final String value;

    private final String description;

    FinanceOrderReconciliationStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FinanceOrderReconciliationStatusEnum getByValue(String value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }
}
