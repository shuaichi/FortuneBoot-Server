package com.fortuneboot.domain.command.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateUserCommand extends AddUserCommand {

    private Long userId;

    @Override
    public String getPassword(){
        return super.getPassword();
    }
}
