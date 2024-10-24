package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneCurrencyMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCurrencyEntity;
import com.fortuneboot.repository.fortune.FortuneCurrencyRepository;
import org.springframework.stereotype.Service;

/**
 * 货币
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 22:48
 **/
@Service
public class FortuneCurrencyRepositoryImpl extends ServiceImpl<FortuneCurrencyMapper, FortuneCurrencyEntity> implements FortuneCurrencyRepository {
}
