package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 单据表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:12
 **/
@Service
@AllArgsConstructor
public class FortuneFinanceOrderRepoImpl
        extends ServiceImpl<FortuneFinanceOrderMapper, FortuneFinanceOrderEntity>
        implements FortuneFinanceOrderRepo {

    @Override
    public List<FortuneFinanceOrderEntity> getUsingFinanceOrderList(Long bookId) {
        LambdaQueryWrapper<FortuneFinanceOrderEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        queryWrapper.eq(FortuneFinanceOrderEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderEntity::getStatus, FinanceOrderStatusEnum.USING.getValue());
        return this.list(queryWrapper);
    }
}
