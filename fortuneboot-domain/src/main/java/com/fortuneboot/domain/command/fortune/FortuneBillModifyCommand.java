package com.fortuneboot.domain.command.fortune;

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
    private Long billId;
}
