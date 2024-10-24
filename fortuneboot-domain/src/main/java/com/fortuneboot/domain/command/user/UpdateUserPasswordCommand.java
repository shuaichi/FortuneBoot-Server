package com.fortuneboot.domain.command.user;

import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class UpdateUserPasswordCommand {

    private Long userId;
    private String newPassword;
    private String oldPassword;

}
