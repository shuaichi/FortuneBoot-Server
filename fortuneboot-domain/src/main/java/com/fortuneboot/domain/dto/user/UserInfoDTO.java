package com.fortuneboot.domain.dto.user;

import com.fortuneboot.domain.dto.RoleDTO;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class UserInfoDTO {

    private UserDTO user;
    private RoleDTO role;

}
