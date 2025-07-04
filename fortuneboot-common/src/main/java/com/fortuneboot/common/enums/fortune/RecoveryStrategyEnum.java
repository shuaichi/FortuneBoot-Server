package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/7/2 20:32
 **/
@Getter
public enum RecoveryStrategyEnum implements BasicEnum<Integer> {
    FULL_RECOVERY(1, "全部补偿"),
    RECENT_N_RECOVERY(2, "仅补偿最近N次"),
    NO_RECOVERY(3, "不补偿")
    ;

    private final Integer value;
    private final String description;

    RecoveryStrategyEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static RecoveryStrategyEnum getEnumByValue(Integer value) {
        for (RecoveryStrategyEnum strategy : values()) {
            if (Objects.equals(strategy.value, value)) {
                return strategy;
            }
        }
        return NO_RECOVERY;
    }
}
