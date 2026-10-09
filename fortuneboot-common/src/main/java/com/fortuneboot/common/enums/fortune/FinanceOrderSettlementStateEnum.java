package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 单据结算状态枚举
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Getter
public enum FinanceOrderSettlementStateEnum implements BasicEnum<String> {

    EMPTY("EMPTY", "空单据"),
    UNSETTLED("UNSETTLED", "未结算"),
    PARTIAL("PARTIAL", "部分结算"),
    SETTLED("SETTLED", "已结清"),
    REVIEW_REQUIRED("REVIEW_REQUIRED", "待核对"),
    ;

    private final String value;

    private final String description;

    FinanceOrderSettlementStateEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FinanceOrderSettlementStateEnum getByValue(String value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }
}
