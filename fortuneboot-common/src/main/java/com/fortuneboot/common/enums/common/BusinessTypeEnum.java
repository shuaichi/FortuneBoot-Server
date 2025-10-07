package com.fortuneboot.common.enums.common;

import com.fortuneboot.common.enums.dictionary.CssTag;
import com.fortuneboot.common.enums.dictionary.Dictionary;
import com.fortuneboot.common.enums.DictionaryEnum;

/**
 * 对应sys_operation_log的business_type
 *
 * @author valarchie
 */
@Dictionary(name = "sysOperationLog.businessType")
public enum BusinessTypeEnum implements DictionaryEnum<Integer> {

    /**
     * 操作类型
     */
    OTHER(0, "其他操作", CssTag.INFO),
    ADD(1, "添加", CssTag.PRIMARY),
    MODIFY(2, "修改", CssTag.PRIMARY),
    DELETE(3, "删除", CssTag.DANGER),
    GRANT(4, "授权", CssTag.PRIMARY),
    EXPORT(5, "导出", CssTag.WARNING),
    IMPORT(6, "导入", CssTag.WARNING),
    FORCE_LOGOUT(7, "强退", CssTag.DANGER),
    CLEAN(8, "清空", CssTag.DANGER),

    // fortune 业务类型
    MOVE_TO_RECYCLE_BIN(9, "移入回收站", CssTag.DANGER),
    PUT_BACK(10, "移出回收站", CssTag.PRIMARY),
    CAN_EXPENSE(11,"可支出", CssTag.PRIMARY),
    CAN_NOT_EXPENSE(12,"不可支出", CssTag.PRIMARY),
    CAN_INCOME(13,"可收入", CssTag.PRIMARY),
    CAN_NOT_INCOME(14,"不可收入", CssTag.PRIMARY),
    CAN_TRANSFER(15,"可转账", CssTag.PRIMARY),
    CAN_NOT_TRANSFER(16,"不可转账", CssTag.PRIMARY),
    CAN_TRANSFER_OUT(17,"可转出", CssTag.PRIMARY),
    CAN_NOT_TRANSFER_OUT(18,"不可转出", CssTag.PRIMARY),
    CAN_TRANSFER_IN(19,"可转入", CssTag.PRIMARY),
    CAN_NOT_TRANSFER_IN(20,"不可转入", CssTag.PRIMARY),
    ENABLE(21,"启用", CssTag.PRIMARY),
    DISABLE(22,"停用", CssTag.PRIMARY),
    INCLUDE(23,"统计", CssTag.PRIMARY),
    EXCLUDE(24,"不统计", CssTag.PRIMARY),
    SET_DEFAULT(25,"设置默认", CssTag.PRIMARY),
    BALANCE_ADJUST(25,"余额调整", CssTag.PRIMARY),
    USING_ORDER(26,"使用单据", CssTag.PRIMARY),
    CLOSE_ORDER(27,"关闭单据", CssTag.WARNING),
    REOPEN_ORDER(28,"重开单据", CssTag.PRIMARY),
    ;

    private final int value;
    private final String description;
    private final String cssTag;

    BusinessTypeEnum(int value, String description, String cssTag) {
        this.value = value;
        this.description = description;
        this.cssTag = cssTag;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String cssTag() {
        return cssTag;
    }

}
