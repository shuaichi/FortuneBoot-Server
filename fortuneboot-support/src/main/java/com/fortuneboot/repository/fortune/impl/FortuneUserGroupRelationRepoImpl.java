package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneUserGroupRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户/分组关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:47
 **/
@Service
public class FortuneUserGroupRelationRepoImpl extends ServiceImpl<FortuneUserGroupRelationMapper, FortuneUserGroupRelationEntity> implements FortuneUserGroupRelationRepo {

    @Override
    public List<FortuneUserGroupRelationEntity> getByGroupId(Long groupId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getGroupId, groupId);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByGroupId(Long groupId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getGroupId,groupId);
        List<FortuneUserGroupRelationEntity> list = this.list(queryWrapper);
        List<Long> ids = list.stream().map(FortuneUserGroupRelationEntity::getUserGroupRelationId).toList();
        this.removeByIds(ids);
    }

    @Override
    public List<FortuneUserGroupRelationEntity> getByUserId() {
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getUserId,loginUser.getUserId());
        return this.list(queryWrapper);
    }

    @Override
    public FortuneUserGroupRelationEntity getByGroupAndUser(Long groupId, Long userId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getGroupId, groupId)
                .eq(FortuneUserGroupRelationEntity::getUserId, userId);
        return this.getOne(queryWrapper);
    }

    @Override
    public Boolean existsByUserId(Long userId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getUserId, userId);
        return this.exists(queryWrapper);
    }

    @Override
    public FortuneUserGroupRelationEntity getDefaultGroupByUser(Long userId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getUserId, userId)
                .eq(FortuneUserGroupRelationEntity::getDefaultGroup, Boolean.TRUE);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<FortuneUserGroupRelationEntity> getDefaultGroupByGroupId(Long groupId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getGroupId, groupId)
                .eq(FortuneUserGroupRelationEntity::getDefaultGroup, Boolean.TRUE);
        return this.list(queryWrapper);
    }
}
