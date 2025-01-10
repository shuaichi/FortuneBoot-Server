package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhangchi118
 * @date 2025/1/10 15:36
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneTagModifyCommand extends FortuneTagAddCommand{

    /**
     * id
     */
    @NotNull
    @Positive
    private Long tagId;
}
