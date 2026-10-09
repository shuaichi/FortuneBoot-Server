package com.fortuneboot.common.enums.fortune;

/**
 * 单据流水操作动作枚举。
 * <p>对外契约仍序列化为枚举名称字符串，禁止在业务代码中手写动作编码。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public enum FinanceOrderBillActionEnum {
    EDIT,
    CONFIRM,
    UNCONFIRM,
    VOID,
    RESTORE,
    COPY
}
