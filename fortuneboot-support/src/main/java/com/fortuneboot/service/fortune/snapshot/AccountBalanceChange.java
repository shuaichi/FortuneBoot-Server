package com.fortuneboot.service.fortune.snapshot;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单对账户余额产生的变动
 *
 * @author zhangchi118
 * @date 2026/8/4
 */
public record AccountBalanceChange(Long accountId, LocalDate tradeDate, BigDecimal amount) {
}
