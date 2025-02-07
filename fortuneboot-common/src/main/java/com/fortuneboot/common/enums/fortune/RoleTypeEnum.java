package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 权限类型枚举
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:43
 **/
@Getter
public enum RoleTypeEnum implements BasicEnum<Integer> {
    OWNER(1, "管理员"),
    ACTOR(2, "协作者"),
    VISITOR(3, "访客"),
    ;

    private final Integer value;

    private final String description;

    RoleTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据value查询枚举
     *
     * @param value
     * @return
     */
    public static RoleTypeEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的角色类型"));
    }

}
