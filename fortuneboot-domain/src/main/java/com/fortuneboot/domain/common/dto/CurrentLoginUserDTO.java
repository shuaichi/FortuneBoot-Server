package com.fortuneboot.domain.common.dto;

import com.fortuneboot.domain.dto.user.UserDTO;
import java.util.Set;

import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class CurrentLoginUserDTO {

    private UserDTO userInfo;
    private String roleKey;
    private Set<String> permissions;


}
