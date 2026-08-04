package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户类型资产分布
 *
 * @author zhangchi118
 * @date 2026/8/4 19:39
 **/
@Data
public class AccountTypeAssetsVo {

    private Integer accountType;

    private String accountTypeName;

    private BigDecimal assets;

    private BigDecimal liabilities;

    private Integer accountCount;
}
