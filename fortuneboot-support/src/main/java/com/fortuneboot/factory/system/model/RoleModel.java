package com.fortuneboot.factory.system.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.exception.error.ErrorCode.Business;
import com.fortuneboot.common.enums.common.StatusEnum;
import com.fortuneboot.domain.command.system.AddRoleCommand;
import com.fortuneboot.domain.command.system.UpdateRoleCommand;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysRoleMenuEntity;
import com.fortuneboot.repository.system.SysRoleMenuRepo;
import com.fortuneboot.repository.system.SysRoleRepo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RoleModel extends SysRoleEntity {

    private List<Long> menuIds;

    private SysRoleRepo roleRepository;
    private SysRoleMenuRepo roleMenuRepository;

    public RoleModel(SysRoleRepo roleRepository, SysRoleMenuRepo roleMenuRepository) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    public RoleModel(SysRoleEntity entity, SysRoleRepo roleRepository, SysRoleMenuRepo roleMenuRepository) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
    }

    public void loadAddCommand(AddRoleCommand command) {
        if (command != null) {
            BeanUtil.copyProperties(command, this, "roleId");
        }
    }

    public void loadUpdateCommand(UpdateRoleCommand command) {
        if (command != null) {
            loadAddCommand(command);
        }
    }

    public void checkRoleNameUnique() {
        if (roleRepository.isRoleNameDuplicated(getRoleId(), getRoleName())) {
            throw new ApiException(ErrorCode.Business.ROLE_NAME_IS_NOT_UNIQUE, getRoleName());
        }
    }

    public void checkRoleCanBeDelete() {
        if (roleRepository.isAssignedToUsers(getRoleId())) {
            throw new ApiException(Business.ROLE_ALREADY_ASSIGN_TO_USER, getRoleName());
        }
    }

    public void checkRoleKeyUnique() {
        if (roleRepository.isRoleKeyDuplicated(getRoleId(), getRoleKey())) {
            throw new ApiException(ErrorCode.Business.ROLE_KEY_IS_NOT_UNIQUE, getRoleKey());
        }
    }

    public void checkRoleAvailable() {
        if (StatusEnum.DISABLE.getValue().equals(getStatus())) {
            throw new ApiException(Business.ROLE_IS_NOT_AVAILABLE, getRoleName());
        }
    }

    @Override
    public boolean insert() {
        super.insert();
        return saveMenus();
    }

    @Override
    public boolean updateById() {
        // 清空之前的角色菜单关联
        cleanOldMenus();
        saveMenus();
        return super.updateById();
    }

    @Override
    public boolean deleteById() {
        // 清空之前的角色菜单关联
        cleanOldMenus();
        return super.deleteById();
    }


    private void cleanOldMenus() {
        LambdaQueryWrapper<SysRoleMenuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRoleMenuEntity::getRoleId, getRoleId());

        roleMenuRepository.remove(queryWrapper);
    }

    private boolean saveMenus() {
        List<SysRoleMenuEntity> list = new ArrayList<>();
        if (getMenuIds() != null) {
            for (Long menuId : getMenuIds()) {
                SysRoleMenuEntity rm = new SysRoleMenuEntity();
                rm.setRoleId(getRoleId());
                rm.setMenuId(menuId);
                list.add(rm);
            }
            return roleMenuRepository.saveBatch(list);
        }
        return false;
    }

}
