package com.fortuneboot.domain.dto.fortune;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 * @author zhangchi118
 * @date 2025/11/28 14:10
 **/
@Data
public class CategoryAmountDTO {
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    /**
     * 构造方法
     */
    public CategoryAmountDTO() {
    }

    /**
     * 构造方法
     *
     * @param categoryId
     * @param amount
     */
    public CategoryAmountDTO(Long categoryId, BigDecimal amount) {
        this.categoryId = categoryId;
        this.amount = amount;
    }
}