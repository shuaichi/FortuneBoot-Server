package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 信用卡额度看板
 *
 * @author zhangchi118
 * @date 2026/8/4 19:40
 **/
@Data
public class CreditCardVo {

    private Long accountId;

    private String accountName;

    private BigDecimal creditLimit;

    private BigDecimal usedAmount;

    private BigDecimal available;

    private BigDecimal usageRate;
}
