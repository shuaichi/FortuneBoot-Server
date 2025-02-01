package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneBookMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账本
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:14
 **/
@Service
public class FortuneBookRepositoryImpl extends ServiceImpl<FortuneBookMapper, FortuneBookEntity> implements FortuneBookRepository {

    @Override
    public List<FortuneBookEntity> getByGroupId(Long groupId) {
        LambdaQueryWrapper<FortuneBookEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBookEntity.class);
        lambdaQueryWrapper.eq(FortuneBookEntity::getGroupId, groupId);
        return this.list(lambdaQueryWrapper);
    }
}
