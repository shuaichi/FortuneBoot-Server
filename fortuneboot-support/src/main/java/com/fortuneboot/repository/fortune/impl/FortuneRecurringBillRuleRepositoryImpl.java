package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneRecurringBillRuleMapper;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:48
 **/
@Service
@AllArgsConstructor
public class FortuneRecurringBillRuleRepositoryImpl extends ServiceImpl<FortuneRecurringBillRuleMapper, FortuneRecurringBillRuleEntity> implements FortuneRecurringBillRuleRepository {

    @Override
    public List<FortuneRecurringBillRuleEntity> getAllEnable() {
        LambdaQueryWrapper<FortuneRecurringBillRuleEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneRecurringBillRuleEntity.class);
        queryWrapper.eq(FortuneRecurringBillRuleEntity::getEnable, Boolean.TRUE);
        return this.list(queryWrapper);
    }
}
