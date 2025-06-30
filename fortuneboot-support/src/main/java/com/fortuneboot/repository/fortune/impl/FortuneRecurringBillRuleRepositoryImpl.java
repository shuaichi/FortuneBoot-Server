package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneRecurringBillRuleMapper;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRule;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:48
 **/
@Service
@AllArgsConstructor
public class FortuneRecurringBillRuleRepositoryImpl extends ServiceImpl<FortuneRecurringBillRuleMapper, FortuneRecurringBillRule> implements FortuneRecurringBillRuleRepository {

}
