package com.fortuneboot.common.enums.fortune;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 权限类型枚举
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:43
 **/
@Getter
public enum RoleTypeEnum {
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
        for (RoleTypeEnum typeEnum : RoleTypeEnum.values()) {
            if (ObjectUtil.equal(value, typeEnum.getValue())) {
                return typeEnum;
            }
        }
        return null;
    }

}
