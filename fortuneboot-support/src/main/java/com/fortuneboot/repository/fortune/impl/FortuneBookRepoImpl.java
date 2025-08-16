package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneBookMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账本
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:14
 **/
@Service
public class FortuneBookRepoImpl extends ServiceImpl<FortuneBookMapper, FortuneBookEntity> implements FortuneBookRepo {

    @Override
    public List<FortuneBookEntity> getByGroupId(Long groupId) {
        LambdaQueryWrapper<FortuneBookEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBookEntity.class);
        lambdaQueryWrapper.eq(FortuneBookEntity::getGroupId, groupId);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public List<FortuneBookEntity> getEnableBookList(Long groupId) {
        LambdaQueryWrapper<FortuneBookEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBookEntity.class);
        lambdaQueryWrapper.eq(FortuneBookEntity::getGroupId, groupId)
                .eq(FortuneBookEntity::getEnable,Boolean.TRUE)
                .eq(FortuneBookEntity::getRecycleBin,Boolean.FALSE);
        return this.list(lambdaQueryWrapper);
    }
}
