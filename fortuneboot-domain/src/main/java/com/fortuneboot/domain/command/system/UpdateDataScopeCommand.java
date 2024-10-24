package com.fortuneboot.domain.command.system;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class UpdateDataScopeCommand {

    @NotNull
    @Positive
    private Long roleId;

    private Integer dataScope;


}
