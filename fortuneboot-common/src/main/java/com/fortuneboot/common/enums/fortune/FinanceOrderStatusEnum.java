package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 20:27
 **/
@Getter
public enum FinanceOrderStatusEnum implements BasicEnum<Integer> {

    INIT(100, "初始化"),
    USING(200,"使用中"),
    CLOSE(1000,"已关闭"),
    ;

    private final Integer value;

    private final String description;

    FinanceOrderStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static String getDescByValue(Integer status) {
        for (FinanceOrderStatusEnum financeOrderStatusEnum : values()) {
            if (Objects.equals(financeOrderStatusEnum.getValue(), status)) {
                return financeOrderStatusEnum.getDescription();
            }
        }
        return StringUtils.EMPTY;
    }
}
