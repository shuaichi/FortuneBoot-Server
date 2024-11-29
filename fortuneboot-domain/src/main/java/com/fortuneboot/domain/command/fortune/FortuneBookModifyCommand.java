package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhangchi118
 * @date 2024/11/29 16:14
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBookModifyCommand extends FortuneBookAddCommand{

    @NotNull
    @Positive
    protected Long bookId;
}
