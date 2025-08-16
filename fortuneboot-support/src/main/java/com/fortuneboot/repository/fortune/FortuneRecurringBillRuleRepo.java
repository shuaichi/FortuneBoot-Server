package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:48
 **/
public interface FortuneRecurringBillRuleRepo extends IService<FortuneRecurringBillRuleEntity> {
    /**
     * 获取所有启用的周期记账规则
     *
     * @return
     */
    List<FortuneRecurringBillRuleEntity> getAllEnable();

}
