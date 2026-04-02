package com.fortuneboot.service.system;

import cn.hutool.core.collection.CollUtil;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.factory.system.factory.RoleModelFactory;
import com.fortuneboot.factory.system.model.RoleModel;
import com.fortuneboot.repository.system.SysMenuRepo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.service.cache.CacheService;
import com.fortuneboot.domain.command.system.AddRoleCommand;
import com.fortuneboot.domain.command.system.UpdateDataScopeCommand;
import com.fortuneboot.domain.command.system.UpdateRoleCommand;
import com.fortuneboot.domain.command.system.UpdateStatusCommand;
import com.fortuneboot.domain.dto.RoleDTO;
import com.fortuneboot.domain.query.system.AllocatedRoleQuery;
import com.fortuneboot.domain.query.system.RoleQuery;
import com.fortuneboot.domain.query.system.UnallocatedRoleQuery;
import com.fortuneboot.domain.dto.user.UserDTO;
import com.fortuneboot.factory.system.model.UserModel;
import com.fortuneboot.factory.system.factory.UserModelFactory;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.repository.system.SysRoleRepo;
import com.fortuneboot.repository.system.SysUserRepo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleModelFactory roleModelFactory;

    private final UserModelFactory userModelFactory;

    private final SysRoleRepo roleRepository;

    private final SysUserRepo userRepository;

    private final SysMenuRepo menuRepository;

    private final CacheService cacheService;


    public PageDTO<RoleDTO> getRoleList(RoleQuery query) {
        Page<SysRoleEntity> page = roleRepository.page(query.toPage(), query.toQueryWrapper());
        List<RoleDTO> records = page.getRecords().stream().map(RoleDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public RoleDTO getRoleInfo(Long roleId) {
        SysRoleEntity byId = roleRepository.getById(roleId);
        RoleDTO roleDTO = new RoleDTO(byId);
        roleDTO.setSelectedMenuList(menuRepository.getMenuIdsByRoleId(roleId));
        return roleDTO;
    }


    public void addRole(AddRoleCommand addCommand) {
        RoleModel roleModel = roleModelFactory.create();
        roleModel.loadAddCommand(addCommand);

        roleModel.checkRoleNameUnique();
        roleModel.checkRoleKeyUnique();

        roleModel.insert();
    }

    public void deleteRoleByBulk(List<Long> roleIds) {
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                RoleModel roleModel = roleModelFactory.loadById(roleId);

                roleModel.checkRoleCanBeDelete();

                roleModel.deleteById();
            }
        }
    }


    public void updateRole(UpdateRoleCommand updateCommand) {
        RoleModel roleModel = roleModelFactory.loadById(updateCommand.getRoleId());
        roleModel.loadUpdateCommand(updateCommand);

        roleModel.checkRoleKeyUnique();
        roleModel.checkRoleNameUnique();

        roleModel.updateById();
        // 角色权限变更，踢出所有使用该角色的用户会话
        invalidateLoginCacheByRoleId(updateCommand.getRoleId());
    }

    public void updateStatus(UpdateStatusCommand command) {
        RoleModel roleModel = roleModelFactory.loadById(command.getRoleId());

        roleModel.setStatus(command.getStatus());

        roleModel.updateById();
        // 角色状态变更（禁用），踢出所有使用该角色的用户会话
        invalidateLoginCacheByRoleId(command.getRoleId());
    }

    public void updateDataScope(UpdateDataScopeCommand command) {
        RoleModel roleModel = roleModelFactory.loadById(command.getRoleId());
        roleModel.setDataScope(command.getDataScope());
        roleModel.updateById();
        // 数据范围变更，踢出所有使用该角色的用户会话
        invalidateLoginCacheByRoleId(command.getRoleId());
    }


    public PageDTO<UserDTO> getAllocatedUserList(AllocatedRoleQuery query) {
        Page<SysUserEntity> page = userRepository.getUserListByRole(query);
        List<UserDTO> dtoList = page.getRecords().stream().map(UserDTO::new).collect(Collectors.toList());
        return new PageDTO<>(dtoList, page.getTotal());
    }

    public PageDTO<UserDTO> getUnallocatedUserList(UnallocatedRoleQuery query) {
        Page<SysUserEntity> page = userRepository.getUserListByRole(query);
        List<UserDTO> dtoList = page.getRecords().stream().map(UserDTO::new).collect(Collectors.toList());
        return new PageDTO<>(dtoList, page.getTotal());
    }

    public void deleteRoleOfUserByBulk(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        for (Long userId : userIds) {
            LambdaUpdateWrapper<SysUserEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(SysUserEntity::getRoleId, null).eq(SysUserEntity::getUserId, userId);

            userRepository.update(updateWrapper);

            CacheCenter.userCache.delete(userId);
            // 用户角色被移除，踢出其登录会话
            cacheService.removeLoginUserByUserId(userId);
        }
    }

    public void addRoleOfUserByBulk(Long roleId, List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }

        RoleModel roleModel = roleModelFactory.loadById(roleId);
        roleModel.checkRoleAvailable();

        for (Long userId : userIds) {
            UserModel user = userModelFactory.loadById(userId);
            user.setRoleId(roleId);
            user.updateById();

            CacheCenter.userCache.delete(userId);
            // 用户角色变更，踢出其旧的登录会话，下次登录将使用新角色权限
            cacheService.removeLoginUserByUserId(userId);
        }
    }

    /**
     * 查找指定角色的所有用户，批量使其登录会话失效
     */
    private void invalidateLoginCacheByRoleId(Long roleId) {
        List<Long> userIds = userRepository.list(
                Wrappers.<SysUserEntity>lambdaQuery()
                        .select(SysUserEntity::getUserId)
                        .eq(SysUserEntity::getRoleId, roleId)
        ).stream().map(SysUserEntity::getUserId).collect(Collectors.toList());

        if (!userIds.isEmpty()) {
            cacheService.removeLoginUsersByUserIds(userIds);
        }
    }

}
