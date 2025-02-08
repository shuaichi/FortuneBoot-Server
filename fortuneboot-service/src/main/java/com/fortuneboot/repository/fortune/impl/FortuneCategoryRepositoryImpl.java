package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneCategoryMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:27
 **/
@Service
public class FortuneCategoryRepositoryImpl extends ServiceImpl<FortuneCategoryMapper, FortuneCategoryEntity> implements FortuneCategoryRepository {
    @Override
    public List<FortuneCategoryEntity> getByIds(List<Long> categoryIds) {
        LambdaQueryWrapper<FortuneCategoryEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        lambdaQueryWrapper.in(FortuneCategoryEntity::getCategoryId, categoryIds);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public List<FortuneCategoryEntity> getEnableCategoryList(Long bookId,Integer billType) {
        LambdaQueryWrapper<FortuneCategoryEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        lambdaQueryWrapper.eq(FortuneCategoryEntity::getBookId, bookId)
                .eq(FortuneCategoryEntity::getEnable, Boolean.TRUE)
                .eq(FortuneCategoryEntity::getRecycleBin,Boolean.FALSE)
                .eq(FortuneCategoryEntity::getCategoryType,billType)
                .orderByAsc(FortuneCategoryEntity::getSort);
        return this.list(lambdaQueryWrapper);
    }
}
