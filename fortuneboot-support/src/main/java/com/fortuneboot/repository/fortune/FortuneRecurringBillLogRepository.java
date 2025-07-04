package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:46
 **/
public interface FortuneRecurringBillLogRepository extends IService<FortuneRecurringBillLogEntity> {

    /**
     * 根据规则ID查询执行日志
     *
     * @param ruleId
     * @return
     */
    List<FortuneRecurringBillLogEntity> getByRuleId(Long ruleId);
}
