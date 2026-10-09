package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 20:19
 **/
@Getter
public enum FinanceOrderTypeEnum implements BasicEnum<Integer> {
    EXPENSE_CLAIM(1, "费用报销单"),
    LOAN_OUT(2, "债权单"),
    LOAN_IN(3, "债务单"),
    ;

    private final Integer value;

    private final String description;

    FinanceOrderTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static String getDescByValue(Integer type) {
        for (FinanceOrderTypeEnum financeOrderTypeEnum : values()) {
            if (Objects.equals(financeOrderTypeEnum.getValue(), type)) {
                return financeOrderTypeEnum.getDescription();
            }
        }
        return StringUtils.EMPTY;
    }
}
