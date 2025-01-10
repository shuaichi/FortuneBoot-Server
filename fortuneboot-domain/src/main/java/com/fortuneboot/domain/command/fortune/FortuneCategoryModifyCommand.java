package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhangchi118
 * @date 2025/1/10 18:46
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryModifyCommand extends FortuneCategoryAddCommand{

    /**
     * id
     */
    @NotNull
    @Positive
    private Long categoryId;
}
