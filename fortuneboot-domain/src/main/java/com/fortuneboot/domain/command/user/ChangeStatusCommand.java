package com.fortuneboot.domain.command.user;

import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class ChangeStatusCommand {

    private Long userId;
    private String status;

}
