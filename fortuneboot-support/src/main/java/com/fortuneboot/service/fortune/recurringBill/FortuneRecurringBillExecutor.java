package com.fortuneboot.service.fortune.recurringBill;

import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.customize.recurringBillLog.RecurringBillLog;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.factory.fortune.FortuneRecurringBillRuleFactory;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import com.fortuneboot.service.fortune.FortuneBillService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 周期记账执行器
 *
 * @author zhangchi118
 * @date 2025/7/10 14:49
 **/
@Slf4j
@Service
@AllArgsConstructor
public class FortuneRecurringBillExecutor {

    private final FortuneRecurringBillRuleFactory fortuneRecurringBillRuleFactory;
    private final FortuneBillService fortuneBillService;
    private final FortuneRecurringBillScheduleService scheduleService;

    /**
     * 执行周期记账
     */
    @RecurringBillLog(value = "执行周期记账")
    @Transactional(rollbackFor = Exception.class)
    public Long executeRecurringBill(Long ruleId, LocalDateTime executionTime) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);

        if (!rule.getEnable()) {
            log.warn("规则已禁用，规则ID: {}", ruleId);
            return null;
        }

        // 检查执行条件
        if (!checkExecutionConditions(rule, executionTime)) {
            return null;
        }

        // 解析账单参数并执行
        String billRequestJson = rule.getBillRequest();
        FortuneBillAddCommand billRequest = JacksonUtil.from(billRequestJson, FortuneBillAddCommand.class);
        billRequest.setTradeTime(executionTime);

        // 执行记账逻辑
        Long billId = fortuneBillService.add(billRequest);

        // 更新执行记录
        updateExecutionRecord(rule, executionTime);

        log.info("周期记账执行成功，规则ID: {}, 执行时间: {}", ruleId, executionTime);
        return billId;
    }

    /**
     * 检查执行条件
     */
    private boolean checkExecutionConditions(FortuneRecurringBillRuleModel rule, LocalDateTime executionTime) {
        // 检查是否超过最大执行次数
        if (rule.checkOverExecutions()) {
            log.info("规则已达到最大执行次数，停止执行，规则ID: {}", rule.getRuleId());
            disableRule(rule);
            return false;
        }

        // 检查是否超过结束日期
        if (rule.checkOverEndDate(executionTime.toLocalDate())) {
            log.info("规则已超过结束日期，停止执行，规则ID: {}", rule.getRuleId());
            disableRule(rule);
            return false;
        }

        // 检查是否在开始日期之前
        if (rule.checkBeforeStartDate(executionTime.toLocalDate())) {
            log.info("规则尚未到开始日期，跳过执行，规则ID: {}", rule.getRuleId());
            return false;
        }

        return true;
    }

    /**
     * 更新执行记录
     */
    private void updateExecutionRecord(FortuneRecurringBillRuleModel rule, LocalDateTime executionTime) {
        rule.setExecutedCount(rule.getExecutedCount() + 1);
        rule.setLastExecutedTime(executionTime);

        // 计算下次执行时间
        LocalDateTime nextTime = scheduleService.calculateNextExecutionTime(rule.getCronExpression());
        if (Objects.nonNull(nextTime)) {
            rule.setNextExecutionTime(nextTime);
        }

        rule.updateById();
    }

    /**
     * 禁用规则
     */
    private void disableRule(FortuneRecurringBillRuleModel rule) {
        scheduleService.deleteJob(rule.getRuleId());
        rule.setEnable(Boolean.FALSE);
        rule.updateById();
    }
}
