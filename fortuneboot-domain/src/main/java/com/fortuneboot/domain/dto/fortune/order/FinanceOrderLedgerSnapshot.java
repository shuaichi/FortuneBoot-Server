package com.fortuneboot.domain.dto.fortune.order;

import java.math.BigDecimal;

/**
 * 单据账本汇总快照（纯计算输出）
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public record FinanceOrderLedgerSnapshot(
    BigDecimal outAmount, BigDecimal inAmount, BigDecimal issuedAmount,
    BigDecimal settledAmount, BigDecimal adjustedAmount, BigDecimal pendingAmount,
    BigDecimal interestIncome, BigDecimal interestExpense, BigDecimal feeAmount,
    BigDecimal netCostOrIncome, int pendingEntryCount, String settlementState) {
}
