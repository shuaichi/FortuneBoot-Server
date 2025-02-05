package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签关系表
 *
 * @author zhangchi118
 * @date 2025/1/29 20:00
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FortuneTagRelationAddCommand {

    /**
     * 账单id
     */
    @NotNull
    @Positive
    private Long billId;

    /**
     * 标签id
     */
    @NotNull
    @Positive
    private Long tagId;
}
