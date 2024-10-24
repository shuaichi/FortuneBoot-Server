package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneUserGroupRelationEntity;
import com.fortuneboot.factory.fortune.model.FortuneUserGroupRelationModel;
import com.fortuneboot.repository.fortune.FortuneUserGroupRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 用户/分组关系
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/11 23:27
 **/
@Component
@RequiredArgsConstructor
public class FortuneUserGroupRelationFactory {

    private final FortuneUserGroupRelationRepository fortuneUserGroupRelationRepository;

    public FortuneUserGroupRelationModel loadById(Long userGroupRelationId) {
        FortuneUserGroupRelationEntity userGroupRelationEntity = fortuneUserGroupRelationRepository.getById(userGroupRelationId);
        if (Objects.isNull(userGroupRelationEntity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, userGroupRelationId, "分组");
        }
        return new FortuneUserGroupRelationModel(userGroupRelationEntity, fortuneUserGroupRelationRepository);
    }

    public FortuneUserGroupRelationModel create(){
        return new FortuneUserGroupRelationModel(fortuneUserGroupRelationRepository);
    }
}
