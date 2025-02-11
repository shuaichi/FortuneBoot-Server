package com.fortuneboot.domain.vo.fortune.bill;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhangchi118
 * @date 2025/2/10 21:02
 **/
@Data
public class BillCategoryAmountVo {

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 金额
     */
    private BigDecimal amount;
}
