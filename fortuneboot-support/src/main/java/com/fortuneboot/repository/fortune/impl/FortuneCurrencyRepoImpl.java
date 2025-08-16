package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneCurrencyMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCurrencyEntity;
import com.fortuneboot.repository.fortune.FortuneCurrencyRepo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 货币
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 22:48
 **/
@Service
public class FortuneCurrencyRepoImpl extends ServiceImpl<FortuneCurrencyMapper, FortuneCurrencyEntity> implements FortuneCurrencyRepo {

    @Override
    public List<FortuneCurrencyEntity> getAll() {
        LambdaQueryWrapper<FortuneCurrencyEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCurrencyEntity.class);
        return this.list(queryWrapper);
    }
}
