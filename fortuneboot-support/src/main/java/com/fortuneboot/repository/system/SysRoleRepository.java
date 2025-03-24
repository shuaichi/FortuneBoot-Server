package com.fortuneboot.repository.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.system.SysMenuEntity;
import com.fortuneboot.domain.entity.system.SysRoleEntity;

import java.util.List;

/**
 * <p>
 * 角色信息表 服务类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
public interface SysRoleRepository extends IService<SysRoleEntity> {


    /**
     * 校验角色名称是否唯一
     *
     * @param roleId   角色Id
     * @param roleName 角色名称
     * @return 结果
     */
    boolean isRoleNameDuplicated(Long roleId, String roleName);

    /**
     * 校验角色权限是否唯一
     *
     * @param roleId  角色Id
     * @param roleKey 角色的Key
     * @return 结果
     */
    boolean isRoleKeyDuplicated(Long roleId, String roleKey);


    /**
     * 检测角色是否分配给用户
     *
     * @param roleId 角色id
     * @return 校验结果
     */
    boolean isAssignedToUsers(Long roleId);

    /**
     * 获取用户的权限列表
     *
     * @param roleId 角色id
     * @return 菜单列表
     */
    List<SysMenuEntity> getMenuListByRoleId(Long roleId);

    /**
     * 获取允许注册的角色
     *
     * @return
     */
    List<SysRoleEntity> getAllowRegisterRoles();
}
