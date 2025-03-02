package com.fortuneboot.repository.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Set;

import com.fortuneboot.domain.entity.system.SearchUserDO;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.dao.system.SysUserMapper;
import com.fortuneboot.repository.system.SysUserRepository;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-16
 */
@Service
public class SysUserRepositoryImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserRepository {


    @Override
    public boolean isUserNameDuplicated(String username) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.baseMapper.exists(queryWrapper);
    }


    @Override
    public boolean isPhoneDuplicated(String phone, Long userId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(userId != null, "user_id", userId)
            .eq("phone_number", phone);
        return baseMapper.exists(queryWrapper);
    }

    @Override
    public boolean isEmailDuplicated(String email, Long userId) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(userId != null, "user_id", userId)
            .eq("email", email);
        return baseMapper.exists(queryWrapper);
    }

    @Override
    public SysRoleEntity getRoleOfUser(Long userId) {
        List<SysRoleEntity> list = baseMapper.getRolesByUserId(userId);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Set<String> getMenuPermissionsForUser(Long userId) {
        return baseMapper.getMenuPermsByUserId(userId);
    }

    @Override
    public SysUserEntity getUserByUserName(String userName) {
        QueryWrapper<SysUserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userName);
        return this.getOne(queryWrapper);
    }

    @Override
    public Page<SysUserEntity> getUserListByRole(AbstractPageQuery<SysUserEntity> query) {
        return baseMapper.getUserListByRole(query.toPage(), query.toQueryWrapper());
    }

    @Override
    public Page<SearchUserDO> getUserList(AbstractPageQuery<SearchUserDO> query) {
        return baseMapper.getUserList(query.toPage(), query.toQueryWrapper());
    }

    @Override
    public List<SysUserEntity> getUsersByIds(List<Long> userIds) {
        LambdaQueryWrapper<SysUserEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(SysUserEntity.class);
        queryWrapper.in(SysUserEntity::getUserId, userIds);
        return this.list(queryWrapper);
    }
}