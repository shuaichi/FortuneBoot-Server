package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:35
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryRelationModel extends FortuneCategoryRelationEntity {

    private FortuneCategoryRelationRepository fortuneCategoryRelationRepository;

    public FortuneCategoryRelationModel(FortuneCategoryRelationRepository repository) {
        this.fortuneCategoryRelationRepository = repository;
    }

    public FortuneCategoryRelationModel(FortuneCategoryRelationEntity entity, FortuneCategoryRelationRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneCategoryRelationRepository = repository;
    }

    public void loadAddCommand(FortuneCategoryRelationAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this,"categoryRelationId");
        }
    }

    public void checkCategoryExist(FortuneCategoryModel fortuneCategoryModel) {
        if (Objects.isNull(fortuneCategoryModel)) {
            ErrorCode.Business business = ErrorCode.Business.BILL_TAG_NOT_EXIST;
            throw new ApiException(business);
        }
    }
}
