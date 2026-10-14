package com.fortuneboot.common.enums.fortune;

/**
 * 单据历史核对问题编码枚举。
 * <p>对外诊断契约使用枚举名称字符串，禁止在业务代码中手写编码。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public enum FinanceOrderReconciliationIssueEnum {
    BACKFILL_FAILED,
    UNKNOWN_ORDER_TYPE,
    UNKNOWN_STATUS,
    CROSS_BOOK,
    UNKNOWN_LEDGER_VERSION,
    UNKNOWN_COMPONENT,
    UNKNOWN_BILL_TYPE,
    AMOUNT_INVALID,
    TRADE_TIME_MISSING,
    TRANSFER_ACCOUNT_INVALID,
    ACCOUNT_MISSING,
    CURRENCY_MISMATCH,
    CLAIM_LEGACY_TYPE_UNKNOWN,
    LEGACY_LOAN_ISOLATED,
    CALCULATION_INVALID,
    CLOSED_NOT_SETTLED,
    ENTRY_AFTER_CLOSE,
    ORPHAN_ORDER
}
