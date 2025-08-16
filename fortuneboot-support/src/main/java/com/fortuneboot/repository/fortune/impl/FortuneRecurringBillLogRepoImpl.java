package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneRecurringBillLogMapper;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import com.fortuneboot.repository.fortune.FortuneRecurringBillLogRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:47
 **/
@Service
@AllArgsConstructor
public class FortuneRecurringBillLogRepoImpl extends ServiceImpl<FortuneRecurringBillLogMapper, FortuneRecurringBillLogEntity> implements FortuneRecurringBillLogRepo {

    @Override
    public List<FortuneRecurringBillLogEntity> getByRuleId(Long ruleId) {
        LambdaQueryWrapper<FortuneRecurringBillLogEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneRecurringBillLogEntity.class);
        queryWrapper.eq(FortuneRecurringBillLogEntity::getRuleId, ruleId);
        return this.list(queryWrapper);
    }
}
