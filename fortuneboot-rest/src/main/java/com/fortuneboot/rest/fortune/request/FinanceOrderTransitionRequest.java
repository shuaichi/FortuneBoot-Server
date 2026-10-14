package com.fortuneboot.rest.fortune.request;

/**
 * 单据生命周期动作请求体：仅幂等ID与期望版本；book/order可选，提供时必须与路径一致。
 *
 * @param requestId      请求幂等ID
 * @param expectedVersion 期望版本号
 * @param bookId         账本id
 * @param orderId        单据id
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public record FinanceOrderTransitionRequest(
        String requestId, Long expectedVersion, Long bookId, Long orderId) {
}
