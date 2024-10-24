package com.fortuneboot.domain.command.user;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@Data
@NoArgsConstructor
public class UpdateUserAvatarCommand {

    public UpdateUserAvatarCommand(Long userId, String avatar) {
        this.userId = userId;
        this.avatar = avatar;
    }

    private Long userId;
    private String avatar;

}
