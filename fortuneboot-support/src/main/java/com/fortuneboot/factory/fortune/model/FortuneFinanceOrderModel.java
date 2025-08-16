package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;

import java.util.Objects;

/**
 * 单据表模型
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:15
 **/
public class FortuneFinanceOrderModel extends FortuneFinanceOrderEntity {

    private final FortuneFinanceOrderRepo fortuneFinanceOrderRepo;

    public FortuneFinanceOrderModel(FortuneFinanceOrderRepo fortuneFinanceOrderRepo) {
        this.fortuneFinanceOrderRepo = fortuneFinanceOrderRepo;
    }

    public FortuneFinanceOrderModel(FortuneFinanceOrderRepo fortuneFinanceOrderRepo, FortuneFinanceOrderEntity entity) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneFinanceOrderRepo = fortuneFinanceOrderRepo;
    }
}
