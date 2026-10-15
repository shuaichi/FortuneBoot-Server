package com.fortuneboot.common.enums.fortune;

/**
 * 单据审计动作枚举。
 * <p>落库与接口均使用枚举名称字符串，禁止在业务代码中手写动作编码。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public enum FinanceOrderAuditActionEnum {
    CREATE,
    MODIFY,
    USING,
    INCLUDE,
    EXCLUDE,
    CLOSE,
    REOPEN,
    REMOVE,
    RECONCILE,
    RECONCILE_CONFIRM
}
