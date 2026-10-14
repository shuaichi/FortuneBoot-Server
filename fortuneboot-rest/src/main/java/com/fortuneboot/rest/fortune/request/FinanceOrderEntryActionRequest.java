package com.fortuneboot.rest.fortune.request;

/**
 * 单据流水动作请求体：仅幂等ID与期望版本；路径ID可选，提供时必须与路径一致。
 *
 * @param requestId      请求幂等ID
 * @param expectedVersion 期望版本号
 * @param bookId         账本id
 * @param orderId        单据id
 * @param billId         流水id
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public record FinanceOrderEntryActionRequest(
        String requestId, Long expectedVersion, Long bookId, Long orderId, Long billId) {
}
