package com.fortuneboot.domain.command.system;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@Data
@NoArgsConstructor
public class UpdateStatusCommand {

    private Long roleId;

    private Integer status;

}
