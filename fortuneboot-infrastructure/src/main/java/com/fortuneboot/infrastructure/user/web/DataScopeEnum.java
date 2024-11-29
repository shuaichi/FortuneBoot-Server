package com.fortuneboot.infrastructure.user.web;

import com.fortuneboot.common.enums.BasicEnum;

/**
 * 对应sys_role表的data_scope字段
 * @author valarchie
 */
public enum DataScopeEnum implements BasicEnum<Integer> {

    /**
     * 数据权限范围
     */
    ALL(1, "所有数据权限"),
    ONLY_SELF(5, "仅本人数据权限");

    private final int value;
    private final String description;

    DataScopeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
