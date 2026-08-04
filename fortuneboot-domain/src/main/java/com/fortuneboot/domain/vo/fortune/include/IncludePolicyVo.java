package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

/**
 * 统计口径说明
 *
 * @author zhangchi118
 * @date 2026/8/4 19:42
 **/
@Data
public class IncludePolicyVo {

    private Boolean excludeTransferFromExpense;

    private Boolean excludeLoanFromExpense;

    private Boolean includeUnconfirmed;
}
