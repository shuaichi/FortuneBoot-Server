package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
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

    private FortuneCategoryRelationRepo fortuneCategoryRelationRepo;

    public FortuneCategoryRelationModel(FortuneCategoryRelationRepo repository) {
        this.fortuneCategoryRelationRepo = repository;
    }

    public FortuneCategoryRelationModel(FortuneCategoryRelationEntity entity, FortuneCategoryRelationRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneCategoryRelationRepo = repository;
    }

    public void loadAddCommand(FortuneCategoryRelationAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this,"categoryRelationId");
        }
    }
}
