package com.fortuneboot.domain.dto.fortune.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单据账本流水入参（纯计算输入）
 *
 * @param billId        流水id，新增未入库流水为空
 * @param billType      流水类型
 * @param component     单据组件
 * @param amount        金额
 * @param tradeTime     交易时间
 * @param confirmed     是否确认
 * @param deleted       是否删除
 * @param voided        是否作废
 * @param ledgerVersion 单据账本版本，新规则流水为1，历史流水为0
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public record FinanceOrderLedgerEntry(
    Long billId, Integer billType, String component, BigDecimal amount,
    LocalDateTime tradeTime, boolean confirmed, boolean deleted,
    boolean voided, int ledgerVersion) {
}
