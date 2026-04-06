package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:25
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FortuneMemberRelationAddCommand {
    @NotNull
    @Positive
    private Long billId;

    @NotNull
    @Positive
    private Long memberId;
}