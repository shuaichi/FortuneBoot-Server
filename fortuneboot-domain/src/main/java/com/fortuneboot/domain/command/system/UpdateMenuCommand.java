package com.fortuneboot.domain.command.system;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateMenuCommand extends AddMenuCommand {

    @NotNull
    private Long menuId;

}
