package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;

import java.util.Set;

/**
 * 孤立流水扫描结果。
 *
 * @param bill       流水
 * @param issueCodes 问题编码
 * @param bookId     账本id
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public record FinanceOrderReconciliationOrphanScan(
        FortuneBillEntity bill, Set<String> issueCodes, Long bookId) {
}
