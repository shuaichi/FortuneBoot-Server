package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneTagMapper;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 交易标签
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:31
 **/
@Service
public class FortuneTagRepositoryImpl extends ServiceImpl<FortuneTagMapper, FortuneTagEntity> implements FortuneTagRepository {
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
}
