package com.fortuneboot.service.fortune.recurringBill;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import com.fortuneboot.job.FortuneRecurringBillJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * 定时任务调度管理服务
 *
 * @author zhangchi118
 * @date 2025/7/10 14:45
 **/
@Slf4j
@Service
@AllArgsConstructor
public class FortuneRecurringBillScheduleService {

    private final Scheduler scheduler;
    private final String GROUP_NAME = "RecurringBillGroup";
    private final String JOB_NAME_PREFIX = "RecurringBill_";

    /**
     * 创建或更新定时任务
     */
    public void scheduleJob(FortuneRecurringBillRuleModel rule) {
        try {
            String jobName = JOB_NAME_PREFIX + rule.getRuleId();
            JobKey jobKey = new JobKey(jobName, GROUP_NAME);

            // 删除已存在的任务
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            // 检查是否需要延迟启动
            LocalDate today = LocalDate.now();
            if (rule.checkBeforeStartDate(today)) {
                scheduleDelayedJob(rule);
                return;
            }

            // 检查是否已经过了结束日期
            if (rule.checkOverEndDate(today)) {
                log.info("规则已过结束日期，不创建任务，规则ID: {}", rule.getRuleId());
                return;
            }

            // 正常创建任务
            createImmediateJob(rule);

        } catch (Exception e) {
            log.error("创建周期记账任务失败，规则ID: {}", rule.getRuleId(), e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_CREATE_JOB_FAILED, rule.getRuleId());
        }
    }

    /**
     * 删除定时任务
     */
    public void deleteJob(Long ruleId) {
        try {
            String jobName = JOB_NAME_PREFIX + ruleId;
            JobKey jobKey = new JobKey(jobName, GROUP_NAME);
            scheduler.deleteJob(jobKey);
            log.info("删除定时任务成功，规则ID: {}", ruleId);
        } catch (SchedulerException e) {
            log.error("删除定时任务失败，规则ID: {}", ruleId, e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_REMOVE_JOB_FAILED, ruleId);
        }
    }

    /**
     * 计算下次执行时间
     */
    public LocalDateTime calculateNextExecutionTime(String cronExpression) {
        try {
            CronExpression cronExpr = new CronExpression(cronExpression);
            // TODO 这里返回的是Date对象，可能存在不同时区环境下出现执行时间偏差，但该问题应该可以接受。
            Date nextTime = cronExpr.getNextValidTimeAfter(new Date());
            return Objects.nonNull(nextTime) ?
                    nextTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        } catch (ParseException e) {
            log.error("计算下次执行时间失败，Cron表达式: {}", cronExpression, e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_CALCULATE_NEXT_EXECUTION_TIME_FAILED);
        }
    }

    /**
     * 创建延迟启动的任务
     */
    private void scheduleDelayedJob(FortuneRecurringBillRuleModel rule) throws SchedulerException, ParseException {
        String jobName = JOB_NAME_PREFIX + rule.getRuleId();

        JobDetail jobDetail = createJobDetail(jobName, rule.getRuleId());

        // 计算startDate当天的第一次执行时间
        LocalDateTime startDateTime = rule.getStartDate().atStartOfDay();
        CronExpression cronExpr = new CronExpression(rule.getCronExpression());
        Date firstExecutionAfterStart = cronExpr.getNextValidTimeAfter(
                Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())
        );

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_Trigger", GROUP_NAME)
                .startAt(firstExecutionAfterStart)
                .withSchedule(CronScheduleBuilder.cronSchedule(rule.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 创建立即启动的任务
     */
    private void createImmediateJob(FortuneRecurringBillRuleModel rule) throws SchedulerException {
        String jobName = JOB_NAME_PREFIX + rule.getRuleId();

        JobDetail jobDetail = createJobDetail(jobName, rule.getRuleId());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_Trigger", GROUP_NAME)
                .withSchedule(CronScheduleBuilder.cronSchedule(rule.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 创建JobDetail
     */
    private JobDetail createJobDetail(String jobName, Long ruleId) {
        return JobBuilder.newJob(FortuneRecurringBillJob.class)
                .withIdentity(jobName, GROUP_NAME)
                .usingJobData("ruleId", ruleId)
                .build();
    }
}