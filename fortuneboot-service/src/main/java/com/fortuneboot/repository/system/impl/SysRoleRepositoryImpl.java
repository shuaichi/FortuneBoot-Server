package com.fortuneboot.repository.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.system.SysMenuEntity;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.dao.system.SysRoleMapper;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.dao.system.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;

import com.fortuneboot.repository.system.SysRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色信息表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
@RequiredArgsConstructor
public class SysRoleRepositoryImpl extends ServiceImpl<SysRoleMapper, SysRoleEntity> implements SysRoleRepository {

    private final SysUserMapper userMapper;

    @Override
    public boolean isRoleNameDuplicated(Long roleId, String roleName) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(roleId != null, "role_id", roleId)
            .eq("role_name", roleName);
        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public boolean isRoleKeyDuplicated(Long roleId, String roleKey) {
        QueryWrapper<SysRoleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(roleId != null, "role_id", roleId)
            .eq("role_key", roleKey);
        return this.baseMapper.exists(queryWrapper);
    }

    @Override
    public boolean isAssignedToUsers(Long roleId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return userMapper.exists(queryWrapper);
    }

    @Override
    public List<SysMenuEntity> getMenuListByRoleId(Long roleId) {
        return baseMapper.getMenuListByRoleId(roleId);
    }

    @Override
    public List<SysRoleEntity> getAllowRegisterRoles() {
        LambdaQueryWrapper<SysRoleEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(SysRoleEntity.class);
        queryWrapper.eq(SysRoleEntity::getAllowRegister,Boolean.TRUE);
        return this.list(queryWrapper);
    }

}
