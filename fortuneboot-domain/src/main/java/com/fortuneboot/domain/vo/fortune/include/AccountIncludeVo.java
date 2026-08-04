package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户维度统计
 *
 * @author zhangchi118
 * @date 2026/8/4 19:39
 **/
@Data
public class AccountIncludeVo {

    private Long accountId;

    private String accountName;

    private Integer accountType;

    private BigDecimal amount;

    private BigDecimal percent;
}
