package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分组
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 18:21
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneGroupModifyCommand extends FortuneGroupAddCommand{

    @NotNull
    @Positive
    private Long groupId;

    /**
     * 默认账本
     */
    private Long defaultBookId;
}
