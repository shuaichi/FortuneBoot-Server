package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.fortune.RoleTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationInviteCommand;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.domain.vo.fortune.FortuneUserGroupRelationVo;
import com.fortuneboot.factory.fortune.factory.FortuneUserGroupRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneUserGroupRelationModel;
import com.fortuneboot.infrastructure.user.AuthenticationUtils;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.fortune.FortuneGroupRepo;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepo;
import com.fortuneboot.repository.system.SysUserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户/分组关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 23:32
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneUserGroupRelationService {

    private final FortuneUserGroupRelationRepo fortuneUserGroupRelationRepo;

    private final FortuneUserGroupRelationFactory fortuneUserGroupRelationFactory;

    private final SysUserRepo userRepository;

    private final FortuneGroupRepo fortuneGroupRepo;

    public void newUserInit(FortuneUserGroupRelationAddCommand addCommand){
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.create();
        relationModel.loadAddCommand(addCommand);
        relationModel.checkRepeat(addCommand.getUserId());
        // 如果不存在分组，则将本分组设置为默认分组
        relationModel.setDefaultGroup(Boolean.TRUE);
        relationModel.insert();
    }

    public void add(FortuneUserGroupRelationAddCommand addCommand) {
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.create();
        relationModel.loadAddCommand(addCommand);
        relationModel.checkRepeat(addCommand.getUserId());
        SystemLoginUser user = AuthenticationUtils.getSystemLoginUser();
        Boolean exists = fortuneUserGroupRelationRepo.existsByUserId(user.getUserId());
        // 如果不存在分组，则将本分组设置为默认分组
        relationModel.setDefaultGroup(!exists);
        relationModel.insert();
    }

    public void inviteUser(FortuneUserGroupRelationInviteCommand inviteCommand) {
        SysUserEntity user = userRepository.getUserByUserName(inviteCommand.getUsername());
        if (Objects.isNull(user)) {
            throw new ApiException(ErrorCode.Business.GROUP_USER_NON_EXIST);
        }
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.create();
        relationModel.setUserId(user.getUserId());
        relationModel.loadInviteCommand(inviteCommand);
        relationModel.checkRepeat(inviteCommand.getUsername());
        relationModel.insert();
    }

    public void removeGroupUser(Long id) {
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.loadById(id);
        if (relationModel.getDefaultGroup()) {
            throw new ApiException(ErrorCode.Business.GROUP_CANNOT_DELETE_DEFAULT_GROUP);
        }
        SystemLoginUser loginUser = AuthenticationUtils.getSystemLoginUser();
        if (Objects.equals(loginUser.getUserId(), relationModel.getUserId())) {
            throw new ApiException(ErrorCode.Business.GROUP_CANNOT_DELETE_SELF);
        }
        relationModel.deleteById();
    }

    public List<FortuneUserGroupRelationVo> getGroupUser(Long groupId) {
        List<FortuneUserGroupRelationEntity> userGroupList = fortuneUserGroupRelationRepo.getByGroupId(groupId);
        List<Long> userIds = userGroupList.stream().map(FortuneUserGroupRelationEntity::getUserId).toList();
        List<SysUserEntity> userEntityList = userRepository.listByIds(userIds);
        Map<Long, SysUserEntity> userEntityMap = userEntityList.stream().collect(Collectors.toMap(SysUserEntity::getUserId, Function.identity()));
        return userGroupList.stream().map(item -> {
            SysUserEntity user = userEntityMap.get(item.getUserId());
            FortuneUserGroupRelationVo userGroup = BeanUtil.copyProperties(item, FortuneUserGroupRelationVo.class);
            userGroup.setUsername(user.getUsername());
            userGroup.setNickname(user.getNickname());
            userGroup.setRoleTypeDesc(RoleTypeEnum.getDescByValue(userGroup.getRoleType()));
            return userGroup;
        }).toList();
    }

    public void setDefaultGroup(Long groupId) {
        List<FortuneUserGroupRelationEntity> groupRelationList = fortuneUserGroupRelationRepo.getByUserId();
        for (FortuneUserGroupRelationEntity relationEntity : groupRelationList) {
            if (Objects.equals(groupId, relationEntity.getGroupId())) {
                FortuneGroupEntity groupEntity = fortuneGroupRepo.getById(groupId);
                if (!groupEntity.getEnable()) {
                    throw new ApiException(ErrorCode.Business.GROUP_CANNOT_SET_UNABLE_GROUP_DEFAULT);
                }
                relationEntity.setDefaultGroup(Boolean.TRUE);
            } else {
                relationEntity.setDefaultGroup(Boolean.FALSE);
            }
        }
        fortuneUserGroupRelationRepo.updateBatchById(groupRelationList);
    }

    public Long getDefaultGroupId() {
        SystemLoginUser user = AuthenticationUtils.getSystemLoginUser();
        FortuneUserGroupRelationEntity entity = fortuneUserGroupRelationRepo.getDefaultGroupByUser(user.getUserId());
        return Objects.isNull(entity)?null:entity.getGroupId();
    }
}
