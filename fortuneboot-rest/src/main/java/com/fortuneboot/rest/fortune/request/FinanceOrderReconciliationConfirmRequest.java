package com.fortuneboot.rest.fortune.request;

/**
 * 历史单据人工确认请求体。
 *
 * @param requestId      请求幂等ID
 * @param expectedVersion 期望版本号
 * @param bookId         账本id
 * @param orderId        单据id
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/15
 */
public record FinanceOrderReconciliationConfirmRequest(
        String requestId, Long expectedVersion, Long bookId, Long orderId) {
}
