package com.fortuneboot.domain.dto.fortune.order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单据看板数据库聚合行。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderSummaryRow {

    private BigDecimal receivableAmount = BigDecimal.ZERO;
    private BigDecimal payableAmount = BigDecimal.ZERO;
    private BigDecimal reimbursementAmount = BigDecimal.ZERO;
    private BigDecimal overdueReceivableAmount = BigDecimal.ZERO;
    private BigDecimal overduePayableAmount = BigDecimal.ZERO;
    private BigDecimal overdueReimbursementAmount = BigDecimal.ZERO;
    private long receivableCount;
    private long payableCount;
    private long reimbursementCount;
}
