package com.fortuneboot.domain.command.fortune;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 15:33
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneAccountModifyCommand extends FortuneAccountAddCommand {

    /**
     * id
     */
    private Long accountId;
}
