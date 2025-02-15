package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortunePayeeMapper;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.repository.fortune.FortunePayeeRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 交易对象
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:21
 **/
@Service
@AllArgsConstructor
public class FortunePayeeRepositoryImpl extends ServiceImpl<FortunePayeeMapper, FortunePayeeEntity> implements FortunePayeeRepository {

    private final FortunePayeeMapper fortunePayeeMapper;

    @Override
    public FortunePayeeEntity getByBookIdAndName(Long bookId, String payeeName) {
        LambdaQueryWrapper<FortunePayeeEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        lambdaQueryWrapper.eq(FortunePayeeEntity::getBookId, bookId)
                .eq(FortunePayeeEntity::getPayeeName, payeeName);
        return this.getOne(lambdaQueryWrapper);
    }

    @Override
    public List<FortunePayeeEntity> getEnablePayeeList(Long bookId, Integer billType) {
        LambdaQueryWrapper<FortunePayeeEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        queryWrapper.eq(FortunePayeeEntity::getBookId, bookId)
                .eq(Objects.equals(billType,BillTypeEnum.EXPENSE.getValue()),FortunePayeeEntity::getCanExpense, Boolean.TRUE)
                .eq(Objects.equals(billType,BillTypeEnum.INCOME.getValue()),FortunePayeeEntity::getCanIncome, Boolean.TRUE)
                .eq(FortunePayeeEntity::getEnable,Boolean.TRUE)
                .eq(FortunePayeeEntity::getRecycleBin,Boolean.FALSE)
                .orderByAsc(FortunePayeeEntity::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public List<FortunePayeeEntity> getByIdList(List<Long> payeeIdList) {
        LambdaQueryWrapper<FortunePayeeEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        queryWrapper.in(FortunePayeeEntity::getPayeeId,payeeIdList);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByBookId(Long bookId) {
        LambdaQueryWrapper<FortunePayeeEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        queryWrapper.eq(FortunePayeeEntity::getBookId,bookId);
        List<FortunePayeeEntity> list = this.list(queryWrapper);
        List<Long> ids = list.stream().map(FortunePayeeEntity::getPayeeId).toList();
        fortunePayeeMapper.deleteBatchIds(ids);
    }

    @Override
    public void removeByBookIds(List<Long> bookIds) {
        LambdaQueryWrapper<FortunePayeeEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        queryWrapper.in(FortunePayeeEntity::getBookId,bookIds);
        List<FortunePayeeEntity> list = this.list(queryWrapper);
        List<Long> ids = list.stream().map(FortunePayeeEntity::getPayeeId).toList();
        fortunePayeeMapper.deleteBatchIds(ids);
    }
}
