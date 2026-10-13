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
     * 分类图标（Iconify 图标名，如 ep:menu）
     */
    private String icon;

    /**
     * 金额
     */
    private BigDecimal amount;
}
