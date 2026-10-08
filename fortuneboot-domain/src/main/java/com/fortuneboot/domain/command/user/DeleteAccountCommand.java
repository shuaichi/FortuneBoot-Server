package com.fortuneboot.domain.command.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 自助注销账号
 *
 * @author valarchie
 */
@Data
public class DeleteAccountCommand {

    @NotBlank(message = "密码不能为空")
    private String password;

}
