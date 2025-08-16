package com.fortuneboot.service.system;

import cn.hutool.core.collection.CollUtil;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.factory.system.factory.RoleModelFactory;
import com.fortuneboot.factory.system.model.RoleModel;
import com.fortuneboot.repository.system.SysMenuRepository;
import com.fortuneboot.service.cache.CacheCenter;
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
import com.fortuneboot.repository.system.SysRoleRepository;
import com.fortuneboot.repository.system.SysUserRepository;
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

    private final SysRoleRepository roleRepository;

    private final SysUserRepository userRepository;

    private final SysMenuRepository menuRepository;


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
    }

    public void updateStatus(UpdateStatusCommand command) {
        RoleModel roleModel = roleModelFactory.loadById(command.getRoleId());

        roleModel.setStatus(command.getStatus());

        roleModel.updateById();
    }

    public void updateDataScope(UpdateDataScopeCommand command) {
        RoleModel roleModel = roleModelFactory.loadById(command.getRoleId());
        roleModel.setDataScope(command.getDataScope());
        roleModel.updateById();
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
        }
    }


}
