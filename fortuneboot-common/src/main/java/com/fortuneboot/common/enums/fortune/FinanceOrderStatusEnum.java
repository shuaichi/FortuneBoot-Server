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

    INIT(100, "草稿"),
    USING(200,"进行中"),
    CLOSE(1000,"已归档"),
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

    public static FinanceOrderStatusEnum getByValue(Integer value) {
        for (FinanceOrderStatusEnum status : values()) {
            if (Objects.equals(status.getValue(), value)) {
                return status;
            }
        }
        return null;
    }

    public boolean isInitial() {
        return this == INIT;
    }

    public boolean isUsing() {
        return this == USING;
    }

    public boolean isClosed() {
        return this == CLOSE;
    }

    /**
     * 可编辑状态：草稿或进行中。
     */
    public boolean isEditable() {
        return this == INIT || this == USING;
    }

    public static boolean isEditableValue(Integer value) {
        FinanceOrderStatusEnum status = getByValue(value);
        return status != null && status.isEditable();
    }
}
