package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortunePayeeMapper;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.repository.fortune.FortunePayeeRepository;
import org.springframework.stereotype.Service;

/**
 * 交易对象
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:21
 **/
@Service
public class FortunePayeeRepositoryImpl extends ServiceImpl<FortunePayeeMapper, FortunePayeeEntity> implements FortunePayeeRepository {
    @Override
    public FortunePayeeEntity getByBookIdAndName(Long bookId, String payeeName) {
        LambdaQueryWrapper<FortunePayeeEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        lambdaQueryWrapper.eq(FortunePayeeEntity::getBookId, bookId)
                .eq(FortunePayeeEntity::getPayeeName, payeeName);
        return this.getOne(lambdaQueryWrapper);
    }
}
