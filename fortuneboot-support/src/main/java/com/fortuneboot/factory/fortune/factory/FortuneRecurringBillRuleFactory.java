package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/7/3 17:01
 **/
@Component
@RequiredArgsConstructor
public class FortuneRecurringBillRuleFactory {

    private final FortuneRecurringBillRuleRepository fortuneRecurringBillRuleRepository;

    public FortuneRecurringBillRuleModel loadById(Long ruleId) {
        FortuneRecurringBillRuleEntity entity = fortuneRecurringBillRuleRepository.getById(ruleId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, ruleId, "周期记账规则");
        }
        return new FortuneRecurringBillRuleModel(entity, fortuneRecurringBillRuleRepository);
    }

    public FortuneRecurringBillRuleModel create() {
        return new FortuneRecurringBillRuleModel(fortuneRecurringBillRuleRepository);
    }

    public List<FortuneRecurringBillRuleModel> loadAllEnable() {
        List<FortuneRecurringBillRuleEntity> list = fortuneRecurringBillRuleRepository.getAllEnable();
        return list.stream()
                .map(item -> new FortuneRecurringBillRuleModel(item, fortuneRecurringBillRuleRepository))
                .toList();
    }

}
