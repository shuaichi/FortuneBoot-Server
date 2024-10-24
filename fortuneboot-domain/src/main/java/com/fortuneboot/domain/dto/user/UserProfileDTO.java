package com.fortuneboot.domain.dto.user;

import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
public class UserProfileDTO {

    public UserProfileDTO(SysUserEntity userEntity, SysRoleEntity roleEntity) {
        if (userEntity != null) {
            this.user = new UserDTO(userEntity);
        }

        if (roleEntity != null) {
            this.roleName = roleEntity.getRoleName();
        }
    }

    private UserDTO user;
    private String roleName;

}
