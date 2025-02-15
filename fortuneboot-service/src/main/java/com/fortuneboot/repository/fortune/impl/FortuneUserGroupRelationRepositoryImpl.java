package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.common.DeleteEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneUserGroupRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepository;
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
public class FortuneUserGroupRelationRepositoryImpl extends ServiceImpl<FortuneUserGroupRelationMapper, FortuneUserGroupRelationEntity> implements FortuneUserGroupRelationRepository {

    @Override
    public List<FortuneUserGroupRelationEntity> getByGroupId(Long groupId) {
        LambdaQueryWrapper<FortuneUserGroupRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneUserGroupRelationEntity.class);
        queryWrapper.eq(FortuneUserGroupRelationEntity::getGroupId, groupId);
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByGroupId(Long groupId) {
        LambdaUpdateWrapper<FortuneUserGroupRelationEntity> updateWrapper = WrapperUtil.getLambdaUpdateWrapper(FortuneUserGroupRelationEntity.class);
        updateWrapper.eq(FortuneUserGroupRelationEntity::getGroupId,groupId)
                .set(FortuneUserGroupRelationEntity::getDeleted, DeleteEnum.INVALID.getValue());
        this.update(updateWrapper);
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
}
