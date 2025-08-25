package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 修改类型
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:21
 **/
@Data
public class FortuneFinanceOrderModifyCommand extends FortuneFinanceOrderAddCommand{

    /**
     * id
     */
    @NotNull
    @Positive
    private Long orderId;
}
