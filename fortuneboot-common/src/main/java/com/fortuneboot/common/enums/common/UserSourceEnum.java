package com.fortuneboot.common.enums.common;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Objects;

/**
 * 用户来源
 *
 * @author zhangchi118
 * @date 2024/11/29 09:42
 **/
@Getter
public enum UserSourceEnum implements BasicEnum<Integer> {
    REGISTER(1, "注册"),
    ADMIN_ADD(2, "超管添加"),
    ADMIN_IMPORT(3, "超管导入"),
    ;
    private final Integer value;

    private final String description;

    UserSourceEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据value获取枚举
     *
     * @param source
     * @return
     */
    public static UserSourceEnum getByValue(Integer source) {
        for (UserSourceEnum userSourceEnum : UserSourceEnum.values()) {
            if (Objects.equals(userSourceEnum.getValue(), source)) {
                return userSourceEnum;
            }
        }
        return null;
    }
}
