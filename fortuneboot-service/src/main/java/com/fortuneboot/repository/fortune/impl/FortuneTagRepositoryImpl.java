package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneTagMapper;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 交易标签
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:31
 **/
@Service
@AllArgsConstructor
public class FortuneTagRepositoryImpl extends ServiceImpl<FortuneTagMapper, FortuneTagEntity> implements FortuneTagRepository {

    private final FortuneTagMapper fortuneTagMapper;

    @Override
    public FortuneTagEntity getByBookIdAndName(Long bookId, String tagName) {
        LambdaQueryWrapper<FortuneTagEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        lambdaQueryWrapper.eq(FortuneTagEntity::getBookId, bookId)
                .eq(FortuneTagEntity::getTagName, tagName);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public List<FortuneTagEntity> getEnableTagList(Long bookId, Integer billType) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.eq(FortuneTagEntity::getBookId, bookId)
                .eq(Objects.equals(billType, BillTypeEnum.EXPENSE.getValue()), FortuneTagEntity::getCanExpense, Boolean.TRUE)
                .eq(Objects.equals(billType, BillTypeEnum.INCOME.getValue()), FortuneTagEntity::getCanIncome, Boolean.TRUE)
                .eq(Objects.equals(billType, BillTypeEnum.TRANSFER.getValue()), FortuneTagEntity::getCanTransfer, Boolean.TRUE)
                .eq(FortuneTagEntity::getEnable, Boolean.TRUE)
                .eq(FortuneTagEntity::getRecycleBin, Boolean.FALSE)
                .orderByAsc(FortuneTagEntity::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public List<FortuneTagEntity> getByIds(List<Long> tagIds) {
        LambdaQueryWrapper<FortuneTagEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        lambdaQueryWrapper.in(FortuneTagEntity::getTagId,tagIds);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public void removeByBookId(Long bookId) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.eq(FortuneTagEntity::getBookId,bookId);
        List<FortuneTagEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneTagEntity::getTagId).toList();
        fortuneTagMapper.deleteBatchIds(ids);
    }

    @Override
    public void removeByBookIds(List<Long> bookIds) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.in(FortuneTagEntity::getBookId,bookIds);
        List<FortuneTagEntity> list = this.list(queryWrapper);
        List<Long> ids = list.stream().map(FortuneTagEntity::getTagId).toList();
        fortuneTagMapper.deleteBatchIds(ids);
    }

    @Override
    public List<FortuneTagEntity> getByParentId(Long parentId) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.eq(FortuneTagEntity::getParentId,parentId);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean existsByTagId(Long tagId) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.eq(FortuneTagEntity::getTagId,tagId);
        return this.exists(queryWrapper);
    }

    @Override
    public Map<Long, List<FortuneTagEntity>> getByParentIds(List<Long> parentIds) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.in(FortuneTagEntity::getParentId,parentIds);
        List<FortuneTagEntity> list = this.list(queryWrapper);
        return list.stream().collect(Collectors.groupingBy(FortuneTagEntity::getParentId));
    }
}
