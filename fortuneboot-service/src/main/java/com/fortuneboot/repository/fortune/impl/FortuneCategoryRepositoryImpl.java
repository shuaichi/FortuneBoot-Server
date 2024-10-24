package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneCategoryMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import org.springframework.stereotype.Service;

/**
 * 分类
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:27
 **/
@Service
public class FortuneCategoryRepositoryImpl extends ServiceImpl<FortuneCategoryMapper, FortuneCategoryEntity> implements FortuneCategoryRepository {
}
