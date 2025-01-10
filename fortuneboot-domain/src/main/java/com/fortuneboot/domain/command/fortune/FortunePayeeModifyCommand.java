package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhangchi118
 * @date 2025/1/10 17:52
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortunePayeeModifyCommand extends FortunePayeeAddCommand{

    /**
     * id
     */
    @NotNull
    @Positive
    private Long payeeId;

}
