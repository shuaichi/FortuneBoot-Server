package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 账单附加费用 Model
 *
 * @author zhangchi118
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBillExtraModel extends FortuneBillExtraEntity {

    private FortuneBillExtraRepo fortuneBillExtraRepo;

    public FortuneBillExtraModel(FortuneBillExtraRepo repository) {
        this.fortuneBillExtraRepo = repository;
    }

    public FortuneBillExtraModel(FortuneBillExtraEntity entity, FortuneBillExtraRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneBillExtraRepo = repository;
    }

    public void loadAddCommand(FortuneBillExtraAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "extraId");
        }
    }
}