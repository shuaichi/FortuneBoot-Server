package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 标签关系表
 *
 * @author zhangchi118
 * @date 2025/1/29 20:12
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneTagRelationModel extends FortuneTagRelationEntity {

    private final FortuneTagRelationRepo fortuneTagRelationRepo;

    public FortuneTagRelationModel(FortuneTagRelationRepo repository) {
        this.fortuneTagRelationRepo = repository;
    }

    public FortuneTagRelationModel(FortuneTagRelationEntity entity, FortuneTagRelationRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneTagRelationRepo = repository;
    }

    public void loadAddCommand(FortuneTagRelationAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "tagRelationId");
        }
    }
}
