package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借贷明细
 *
 * @author zhangchi118
 * @date 2026/8/4 19:41
 **/
@Data
public class LoanDetailVo {

    private String payeeName;

    private BigDecimal amount;

    private LocalDateTime lastTradeTime;
}
