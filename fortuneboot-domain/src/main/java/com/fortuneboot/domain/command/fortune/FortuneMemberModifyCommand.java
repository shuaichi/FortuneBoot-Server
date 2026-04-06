package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:25
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneMemberModifyCommand extends FortuneMemberAddCommand {
    @NotNull
    @Positive
    private Long memberId;
}