package com.fortuneboot.factory.system.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysRoleMenuEntity;
import com.fortuneboot.factory.system.model.RoleModel;
import com.fortuneboot.repository.system.SysRoleMenuRepository;
import com.fortuneboot.repository.system.SysRoleRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class RoleModelFactory {

    private final SysRoleRepository roleRepository;

    private final SysRoleMenuRepository roleMenuRepository;

    public RoleModel loadById(Long roleId) {
        SysRoleEntity byId = roleRepository.getById(roleId);
        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, roleId, "角色");
        }

        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, roleId);
        List<Long> menuIds = roleMenuRepository.list(queryWrapper).stream().map(SysRoleMenuEntity::getMenuId)
            .collect(Collectors.toList());

        RoleModel roleModel = new RoleModel(byId, roleRepository, roleMenuRepository);
        roleModel.setMenuIds(menuIds);
        return roleModel;
    }

    public RoleModel create() {
        return new RoleModel(roleRepository, roleMenuRepository);
    }


}
