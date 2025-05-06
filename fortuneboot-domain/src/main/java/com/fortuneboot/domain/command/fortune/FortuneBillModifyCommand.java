package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 23:15
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBillModifyCommand extends FortuneBillAddCommand{

    /**
     * id
     */
    @NotNull
    @Positive
    private Long billId;
}
