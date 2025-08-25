package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * 账单流水类型枚举
 * 1、收入;2、支出;3、转账;4、余额调整;5、盈利;6、亏损
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:21
 **/
@Getter
public enum BillTypeEnum implements BasicEnum<Integer> {
    EXPENSE(1, "支出"),
    INCOME(2, "收入"),
    TRANSFER(3, "转账"),
    ADJUST(4, "余额调整"),
    PROFIT(5, "盈利"),
    LOSS(6, "亏损"),
    ADVANCE(7, "垫付"),
    REIMBURSE(8, "报销"),
    LOAN_OUT(9, "借出"),
    LOAN_RECOVER(10, "收回"),
    LOAN_IN(11, "借入"),
    LOAN_REPAY(12, "归还"),
    ;

    private final Integer value;

    private final String description;

    BillTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BillTypeEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }

    public static String getDescByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .map(BillTypeEnum::getDescription)
                .orElse(StringUtils.EMPTY);
    }
}
