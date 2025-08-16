package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/1/10 18:41
 **/
@Component
@RequiredArgsConstructor
public class FortuneCategoryFactory {

    private final FortuneCategoryRepository fortuneCategoryRepository;

    public FortuneCategoryModel create() {
        return new FortuneCategoryModel(fortuneCategoryRepository);
    }

    public FortuneCategoryModel loadById(Long categoryId) {
        FortuneCategoryEntity fortuneCategoryEntity = fortuneCategoryRepository.getById(categoryId);
        if (Objects.isNull(fortuneCategoryEntity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, categoryId, "分类");
        }
        return new FortuneCategoryModel(fortuneCategoryEntity, fortuneCategoryRepository);
    }

    public List<FortuneCategoryModel> loadByIds(List<Long> categoryIds) {
        List<FortuneCategoryEntity> entities = fortuneCategoryRepository.getByIds(categoryIds);
        if (CollectionUtils.size(entities) != CollectionUtils.size(categoryIds)){
            List<Long> list = entities.stream().map(FortuneCategoryEntity::getCategoryId).toList();
            Collection<Long> subtracts = CollectionUtils.subtract(categoryIds, list);
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, subtracts.toString(), "分类");
        }
        return entities.stream().map(item -> new FortuneCategoryModel(item, fortuneCategoryRepository)).toList();
    }
}
