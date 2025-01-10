package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
