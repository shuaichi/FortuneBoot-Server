package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepo;
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

    private final FortuneRecurringBillRuleRepo fortuneRecurringBillRuleRepo;

    public FortuneRecurringBillRuleModel loadById(Long ruleId) {
        FortuneRecurringBillRuleEntity entity = fortuneRecurringBillRuleRepo.getById(ruleId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, ruleId, "周期记账规则");
        }
        return new FortuneRecurringBillRuleModel(entity, fortuneRecurringBillRuleRepo);
    }

    public FortuneRecurringBillRuleModel create() {
        return new FortuneRecurringBillRuleModel(fortuneRecurringBillRuleRepo);
    }

    public List<FortuneRecurringBillRuleModel> loadAllEnable() {
        List<FortuneRecurringBillRuleEntity> list = fortuneRecurringBillRuleRepo.getAllEnable();
        return list.stream()
                .map(item -> new FortuneRecurringBillRuleModel(item, fortuneRecurringBillRuleRepo))
                .toList();
    }

}
