package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneGroupMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.repository.fortune.FortuneGroupRepo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分组
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:12
 **/
@Service
public class FortuneGroupRepoImpl extends ServiceImpl<FortuneGroupMapper, FortuneGroupEntity> implements FortuneGroupRepo {

    @Override
    public List<FortuneGroupEntity> getEnableByGroupIds(List<Long> groupIds) {
        LambdaQueryWrapper<FortuneGroupEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneGroupEntity.class);
        lambdaQueryWrapper.eq(FortuneGroupEntity::getEnable, Boolean.TRUE)
                .in(FortuneGroupEntity::getGroupId, groupIds);
        return this.list(lambdaQueryWrapper);
    }
}
