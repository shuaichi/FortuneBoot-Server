package com.fortuneboot.domain.vo.fortune.order;

import lombok.Data;

/**
 * 单据关联流水分类金额VO（V2金额为十进制字符串）
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderCategoryAmountVo {

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
    private String amount;
}
