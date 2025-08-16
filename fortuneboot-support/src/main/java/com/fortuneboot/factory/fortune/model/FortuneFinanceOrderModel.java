package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepository;

import java.util.Objects;

/**
 * 单据表模型
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:15
 **/
public class FortuneFinanceOrderModel extends FortuneFinanceOrderEntity {

    private final FortuneFinanceOrderRepository fortuneFinanceOrderRepository;

    public FortuneFinanceOrderModel(FortuneFinanceOrderRepository fortuneFinanceOrderRepository) {
        this.fortuneFinanceOrderRepository = fortuneFinanceOrderRepository;
    }

    public FortuneFinanceOrderModel(FortuneFinanceOrderRepository fortuneFinanceOrderRepository,FortuneFinanceOrderEntity entity) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneFinanceOrderRepository = fortuneFinanceOrderRepository;
    }
}
