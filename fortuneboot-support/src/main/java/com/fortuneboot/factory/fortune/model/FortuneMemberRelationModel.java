package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.command.fortune.FortuneMemberRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:33
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneMemberRelationModel extends FortuneMemberRelationEntity {

    private final FortuneMemberRelationRepo fortuneMemberRelationRepo;

    public FortuneMemberRelationModel(FortuneMemberRelationRepo repository) {
        this.fortuneMemberRelationRepo = repository;
    }

    public void loadAddCommand(FortuneMemberRelationAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "memberRelationId");
        }
    }
}
