package com.fortuneboot.service.fortune.order.reconciliation;

import java.util.List;

/**
 * 历史单据核对只读扫描结果。
 *
 * @param orders  单据扫描结果
 * @param orphans 孤立流水扫描结果
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public record FinanceOrderReconciliationScan(
        List<FinanceOrderReconciliationOrderScan> orders,
        List<FinanceOrderReconciliationOrphanScan> orphans) {
}
