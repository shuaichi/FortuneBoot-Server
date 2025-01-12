package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.command.fortune.FortuneGroupAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneGroupModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.repository.fortune.FortuneGroupRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 分组模型
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 18:38
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneGroupModel extends FortuneGroupEntity {

    private FortuneGroupRepository fortuneGroupRepository;

    public FortuneGroupModel(FortuneGroupRepository repository) {
        this.fortuneGroupRepository = repository;
    }

    public FortuneGroupModel(FortuneGroupEntity entity, FortuneGroupRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneGroupRepository = repository;
    }

    public void loadAddCommand(FortuneGroupAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this,"groupId");
        }
    }

    public void loadModifyCommand(FortuneGroupModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }
}
