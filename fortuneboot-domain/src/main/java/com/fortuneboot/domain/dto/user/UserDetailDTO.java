package com.fortuneboot.domain.dto.user;

import com.fortuneboot.domain.dto.RoleDTO;
import java.util.List;
import java.util.Set;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class UserDetailDTO {

    private UserDTO user;

    /**
     * 返回所有role
     */
    private List<RoleDTO> roleOptions;

    private Long roleId;

    private Set<String> permissions;

}
