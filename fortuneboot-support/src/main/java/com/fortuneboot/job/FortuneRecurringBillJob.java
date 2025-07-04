package com.fortuneboot.job;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.service.fortune.FortuneRecurringBillService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

/**
 * @author zhangchi118
 * @date 2025/7/3 16:58
 **/
@Slf4j
@Component
@AllArgsConstructor
public class FortuneRecurringBillJob implements Job {

    private FortuneRecurringBillService fortuneRecurringBillService;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long ruleId = dataMap.getLong("ruleId");

        try {
            log.info("开始执行周期记账任务，规则ID: {}", ruleId);
            fortuneRecurringBillService.executeRecurringBill(ruleId);
            log.info("周期记账任务执行完成，规则ID: {}", ruleId);
        } catch (Exception e) {
            log.error("周期记账任务执行失败，规则ID: {}", ruleId, e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_EXECUTION_FAILED, ruleId);
        }
    }
}
