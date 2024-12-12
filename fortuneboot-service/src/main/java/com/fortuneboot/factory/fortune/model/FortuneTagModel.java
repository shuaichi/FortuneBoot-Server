package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 账本标签模型
 *
 * @author zhangchi118
 * @date 2024/12/11 16:17
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneTagModel extends FortuneTagEntity {

    private FortuneTagRepository fortuneTagRepository;

    public FortuneTagModel(FortuneTagRepository fortuneTagRepository){
        this.fortuneTagRepository = fortuneTagRepository;
    }

    public FortuneTagModel(FortuneTagEntity entity, FortuneTagRepository fortuneTagRepository){
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneTagRepository = fortuneTagRepository;
    }
}
