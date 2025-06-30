package com.fortuneboot.service.fortune;

import com.fortuneboot.repository.fortune.FortuneRecurringBillLogRepository;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 周期记账服务
 *
 * @author zhangchi118
 * @date 2025/6/30 20:49
 **/
@Slf4j
@Service
@AllArgsConstructor
public class FortuneRecurringBillService {

    private final FortuneRecurringBillRuleRepository  fortuneRecurringBillRuleRepository;

    private final FortuneRecurringBillLogRepository fortuneRecurringBillLogRepository;


}
