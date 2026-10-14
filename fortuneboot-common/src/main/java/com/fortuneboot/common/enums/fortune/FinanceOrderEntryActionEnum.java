package com.fortuneboot.common.enums.fortune;

/**
 * 单据入口动作枚举。
 * <p>对外契约仍序列化为枚举名称字符串，禁止在业务代码中手写动作编码。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public enum FinanceOrderEntryActionEnum {
    ADVANCE,
    REIMBURSE,
    LOAN_OUT,
    LOAN_RECOVER,
    LOAN_IN,
    LOAN_REPAY,
    INTEREST_IN,
    INTEREST_OUT,
    FEE,
    WRITE_OFF
}
