package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneUserGroupRelationModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.domain.vo.fortune.FortuneUserGroupRelationVo;
import com.fortuneboot.factory.fortune.FortuneUserGroupRelationFactory;
import com.fortuneboot.factory.fortune.model.FortuneUserGroupRelationModel;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepository;
import com.fortuneboot.repository.system.SysUserRepository;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private final FortuneUserGroupRelationRepository fortuneUserGroupRelationRepository;

    private final FortuneUserGroupRelationFactory fortuneUserGroupRelationFactory;

    private final SysUserRepository userRepository;

    public void addFortuneUserGroupRelation(FortuneUserGroupRelationAddCommand addCommand) {
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.create();
        relationModel.loadAddCommand(addCommand);
        relationModel.insert();
    }

    public void modifyFortuneUserGroupRelation(FortuneUserGroupRelationModifyCommand modifyCommand) {
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.loadById(modifyCommand.getUserGroupRelationId());
        relationModel.loadModifyCommand(modifyCommand);
        relationModel.updateById();
    }

    public void removeFortuneUserGroupRelation(Long userGroupRelationId) {
        FortuneUserGroupRelationModel relationModel = fortuneUserGroupRelationFactory.loadById(userGroupRelationId);
        if (relationModel.getDefaultGroup()) {
            throw new ApiException(ErrorCode.Business.GROUP_CANNOT_DELETE_DEFAULT_GROUP);
        }
        relationModel.deleteById();
    }

    public List<FortuneUserGroupRelationVo> getUserGroupRelationByGroupId(Long groupId) {
        List<FortuneUserGroupRelationEntity> userGroupRelationEntityList = fortuneUserGroupRelationRepository.getByGroupId(groupId);
        List<Long> userIds = userGroupRelationEntityList.stream().map(FortuneUserGroupRelationEntity::getUserId).toList();
        List<SysUserEntity> userEntityList = userRepository.listByIds(userIds);
        Map<Long, String> userIdMapName = userEntityList.stream().collect(Collectors.toMap(SysUserEntity::getUserId, SysUserEntity::getUsername));
        return userGroupRelationEntityList.stream().map(item -> {
            FortuneUserGroupRelationVo fortuneUserGroupRelationVo = BeanUtil.copyProperties(item, FortuneUserGroupRelationVo.class);
            fortuneUserGroupRelationVo.setUserName(userIdMapName.get(item.getUserId()));
            return fortuneUserGroupRelationVo;
        }).toList();
    }

    public void setDefaultGroup(Long groupId) {
        List<FortuneUserGroupRelationEntity> groupRelationList = fortuneUserGroupRelationRepository.getByUserId();
        for (FortuneUserGroupRelationEntity relationEntity : groupRelationList) {
            if (Objects.equals(groupId, relationEntity.getUserGroupRelationId())) {
                relationEntity.setDefaultGroup(Boolean.TRUE);
            } else {
                relationEntity.setDefaultGroup(Boolean.FALSE);
            }
        }
        fortuneUserGroupRelationRepository.updateBatchById(groupRelationList);
    }
}
