package com.fortuneboot.domain.command.user;

import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class ResetPasswordCommand {

    private Long userId;
    private String password;

}
