package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationInviteCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 用户/分组关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 23:06
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneUserGroupRelationModel extends FortuneUserGroupRelationEntity {

    private FortuneUserGroupRelationRepository fortuneUserGroupRelationRepository;

    public FortuneUserGroupRelationModel(FortuneUserGroupRelationRepository repository) {
        this.fortuneUserGroupRelationRepository = repository;
    }

    public FortuneUserGroupRelationModel(FortuneUserGroupRelationEntity entity, FortuneUserGroupRelationRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneUserGroupRelationRepository = repository;
    }

    public void loadAddCommand(FortuneUserGroupRelationAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "userGroupRelationId");
        }
    }

    public void loadInviteCommand(FortuneUserGroupRelationInviteCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "userGroupRelationId");
        }
    }

    public void loadModifyCommand(FortuneUserGroupRelationModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkRepeat(String username) {
        FortuneUserGroupRelationEntity entity = fortuneUserGroupRelationRepository.getByGroupAndUser(this.getGroupId(), this.getUserId());
        if (Objects.nonNull(entity)) {
            throw new ApiException(ErrorCode.Business.GROUP_USER_ALREADY_EXIST, username);
        }
    }

    public void checkRepeat(Long userId) {
        FortuneUserGroupRelationEntity entity = fortuneUserGroupRelationRepository.getByGroupAndUser(this.getGroupId(), userId);
        if (Objects.nonNull(entity)) {
            throw new ApiException(ErrorCode.Business.GROUP_USER_ALREADY_EXIST, entity.getUserId());
        }
    }
}
