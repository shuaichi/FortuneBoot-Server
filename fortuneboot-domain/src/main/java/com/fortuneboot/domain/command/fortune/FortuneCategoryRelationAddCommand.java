package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:48
 **/
@Data
public class FortuneCategoryRelationAddCommand {

    /**
     * 账单id
     */
    @NotNull
    @Positive
    private Long billId;

    /**
     * 分类id
     */
    @NotNull
    @Positive
    private Long categoryId;

    /**
     * 金额
     */
    @NotNull
    private BigDecimal amount;
}
