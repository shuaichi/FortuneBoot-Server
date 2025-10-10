package com.fortuneboot.domain.command.user;

import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class UpdateProfileCommand {

    private Long userId;

    private Integer sex;
    private String nickname;
    private String phoneNumber;
    private String email;

}
