package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

/**
 * 账单流水类型枚举
 * 1、收入;2、支出;3、转账;4、余额调整;5、盈利;6、亏损
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:21
 **/
@Getter
public enum BalanceFlowTypeEnum implements BasicEnum<Integer> {
    INCOME(1, "收入"),
    EXPENSE(2, "支出"),
    TRANSFER(3, "转账"),
    ADJUST(4, "余额调整"),
    PROFIT(5, "盈利"),
    LOSS(6, "亏损"),
    ;

    private final Integer value;

    private final String description;

    BalanceFlowTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
