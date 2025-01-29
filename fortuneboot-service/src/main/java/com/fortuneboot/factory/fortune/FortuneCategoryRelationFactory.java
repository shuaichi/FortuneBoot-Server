package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.factory.fortune.model.FortuneCategoryRelationModel;
import com.fortuneboot.factory.fortune.model.FortuneGroupModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/1/29 23:34
 **/
@Component
@RequiredArgsConstructor
public class FortuneCategoryRelationFactory {

    private final FortuneCategoryRelationRepository fortuneCategoryRelationRepository;

    public FortuneCategoryRelationModel create(){
        return new FortuneCategoryRelationModel(fortuneCategoryRelationRepository);
    }

    public FortuneCategoryRelationModel loadById(Long groupId) {
        FortuneCategoryRelationEntity entity = fortuneCategoryRelationRepository.getById(groupId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, groupId, "分类关系");
        }
        return new FortuneCategoryRelationModel(entity, fortuneCategoryRelationRepository);
    }
}
