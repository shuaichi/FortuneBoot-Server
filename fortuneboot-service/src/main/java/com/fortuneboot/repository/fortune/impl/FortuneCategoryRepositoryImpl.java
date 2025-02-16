package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneCategoryMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:27
 **/
@Service
@AllArgsConstructor
public class FortuneCategoryRepositoryImpl extends ServiceImpl<FortuneCategoryMapper, FortuneCategoryEntity> implements FortuneCategoryRepository {

    private final FortuneCategoryMapper fortuneCategoryMapper;

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

    @Override
    public void removeByBookId(Long bookId) {
        LambdaQueryWrapper<FortuneCategoryEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        queryWrapper.eq(FortuneCategoryEntity::getBookId,bookId);
        List<FortuneCategoryEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneCategoryEntity::getCategoryId).toList();
        fortuneCategoryMapper.deleteBatchIds(ids);
    }

    @Override
    public void removeByBookIds(List<Long> bookIds) {
        LambdaQueryWrapper<FortuneCategoryEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        queryWrapper.in(FortuneCategoryEntity::getBookId,bookIds);
        List<FortuneCategoryEntity> list = this.list(queryWrapper);
        List<Long> ids = list.stream().map(FortuneCategoryEntity::getCategoryId).toList();
        fortuneCategoryMapper.deleteBatchIds(ids);
    }

    @Override
    public List<FortuneCategoryEntity> getByParentId(Long parentId) {
        LambdaQueryWrapper<FortuneCategoryEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        queryWrapper.eq(FortuneCategoryEntity::getParentId,parentId);
        return this.list(queryWrapper);
    }

    @Override
    public Map<Long, List<FortuneCategoryEntity>> getByParentIds(List<Long> parentIds) {
        LambdaQueryWrapper<FortuneCategoryEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        queryWrapper.in(FortuneCategoryEntity::getParentId,parentIds);
        List<FortuneCategoryEntity> list = this.list(queryWrapper);
        return list.stream().collect(Collectors.groupingBy(FortuneCategoryEntity::getParentId));
    }
}
