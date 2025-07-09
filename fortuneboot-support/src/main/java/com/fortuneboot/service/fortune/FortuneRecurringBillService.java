package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.RecoveryStrategyEnum;
import com.fortuneboot.common.enums.fortune.RecurringBillLogStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.domain.query.fortune.FortuneRecurringBillRuleQuery;
import com.fortuneboot.factory.fortune.FortuneRecurringBillRuleFactory;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import com.fortuneboot.job.FortuneRecurringBillJob;
import com.fortuneboot.repository.fortune.FortuneRecurringBillLogRepository;
import com.fortuneboot.repository.fortune.FortuneRecurringBillRuleRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    private final FortuneRecurringBillRuleFactory fortuneRecurringBillRuleFactory;

    private final FortuneRecurringBillRuleRepository fortuneRecurringBillRuleRepository;

    private final FortuneRecurringBillLogRepository fortuneRecurringBillLogRepository;

    private final Scheduler scheduler;

    private final FortuneBillService fortuneBillService;

    private final String GROUP_NAME = "RecurringBillGroup";

    private final String JOB_NAME_PREFIX = "RecurringBill_";


    public IPage<FortuneRecurringBillRuleEntity> getRulePage(FortuneRecurringBillRuleQuery query) {
        return fortuneRecurringBillRuleRepository.page(query.toPage(), query.addQueryCondition());
    }


    public List<FortuneRecurringBillLogEntity> getLogByRuleId(Long ruleId) {
        return fortuneRecurringBillLogRepository.getByRuleId(ruleId);
    }


    /**
     * 动态添加新规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void addNewRule(FortuneRecurringBillRuleAddCommand addCommand) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.create();
        rule.loadAddCommand(addCommand);
        rule.checkCronValid();
        this.scheduleJob(rule);
    }

    /**
     * 动态更新规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void modifyRule(FortuneRecurringBillRuleModifyCommand modifyCommand) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(modifyCommand.getRuleId());
        rule.loadModifyCommand(modifyCommand);
        rule.checkCronValid();
        rule.checkBookId(modifyCommand.getBookId());
        this.scheduleJob(rule);
    }

    /**
     * 动态删除规则
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRule(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.checkBookId(bookId);
        rule.deleteById();
        this.deleteJob(ruleId);
    }

    /**
     * 启用规则
     */
    public void enableRule(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.checkBookId(bookId);
        rule.setEnable(Boolean.TRUE);
        rule.updateById();
        this.scheduleJob(rule);
    }

    /**
     * 禁用规则
     */
    public void disableRule(Long bookId, Long ruleId) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.checkBookId(bookId);
        rule.setEnable(Boolean.FALSE);
        rule.updateById();
        this.deleteJob(ruleId);
    }

    /**
     * 应用启动时初始化定时任务
     */
    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void initRecurringBills() {
        log.info("开始初始化周期记账任务...");

        // 查询所有启用的规则
        List<FortuneRecurringBillRuleModel> rules = fortuneRecurringBillRuleFactory.loadAllEnable();

        for (FortuneRecurringBillRuleModel rule : rules) {
            // 执行补偿逻辑
            this.performRecovery(rule);

            // 创建定时任务
            this.scheduleJob(rule);
        }

        log.info("周期记账任务初始化完成，共加载{}个任务", rules.size());

    }

    /**
     * 执行补偿逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    void performRecovery(FortuneRecurringBillRuleModel rule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastCheck = rule.getLastRecoveryCheck();

        if (Objects.isNull(lastCheck)) {
            lastCheck = rule.getCreateTime();
        }

        RecoveryStrategyEnum strategy = RecoveryStrategyEnum.getEnumByValue(rule.getRecoveryStrategy());
        if (Objects.equals(strategy, RecoveryStrategyEnum.NO_RECOVERY)) {
            this.updateRecoveryCheckTime(rule.getRuleId(), now);
            return;
        }

        // 计算应该执行但未执行的时间点
        List<LocalDateTime> missedExecutions = this.calculateMissedExecutions(rule, lastCheck, now);

        if (missedExecutions.isEmpty()) {
            this.updateRecoveryCheckTime(rule.getRuleId(), now);
            return;
        }

        // 根据策略过滤需要补偿的执行点
        List<LocalDateTime> toRecover = this.filterRecoveryExecutions(missedExecutions, strategy, rule.getMaxRecoveryCount());

        log.info("规则[{}]需要补偿{}次执行", rule.getRuleName(), toRecover.size());

        // 执行补偿
        for (LocalDateTime executionTime : toRecover) {
            this.executeRecurringBillWithTime(rule.getRuleId(), executionTime);
        }

        this.updateRecoveryCheckTime(rule.getRuleId(), now);
    }

    /**
     * 计算错过的执行时间点
     */
    private List<LocalDateTime> calculateMissedExecutions(FortuneRecurringBillRuleModel rule, LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> missed = new ArrayList<>();

        try {
            CronExpression cronExpr = new CronExpression(rule.getCronExpression());
            LocalDateTime current = from;

            while (current.isBefore(to)) {
                Date nextTime = cronExpr.getNextValidTimeAfter(Date.from(current.atZone(ZoneId.systemDefault()).toInstant()));
                if (Objects.isNull(nextTime)) {
                    break;
                }

                LocalDateTime nextExecution = nextTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                if (nextExecution.isAfter(to)) {
                    break;
                }

                // 检查是否在有效期内
                if (isInValidPeriod(rule, nextExecution)) {
                    missed.add(nextExecution);
                }

                current = nextExecution;
            }
        } catch (ParseException e) {
            log.error("解析Cron表达式失败: {}", rule.getCronExpression(), e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_CRON_EXPRESSION_PARSE_FAILED, rule.getCronExpression());
        }

        return missed;
    }

    /**
     * 根据策略过滤需要补偿的执行点
     */
    private List<LocalDateTime> filterRecoveryExecutions(List<LocalDateTime> missed, RecoveryStrategyEnum strategy, Long maxCount) {
        if (Objects.equals(strategy, RecoveryStrategyEnum.FULL_RECOVERY)) {
            return missed;
        } else if (Objects.equals(strategy, RecoveryStrategyEnum.RECENT_N_RECOVERY) && Objects.nonNull(maxCount)) {
            return missed.stream()
                    .sorted(Comparator.reverseOrder())
                    .limit(maxCount)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 检查时间是否在有效期内
     */
    private boolean isInValidPeriod(FortuneRecurringBillRuleModel rule, LocalDateTime time) {
        LocalDate timeDate = time.toLocalDate();

        if (Objects.nonNull(rule.getStartDate()) && timeDate.isBefore(rule.getStartDate())) {
            return Boolean.FALSE;
        }

        if (Objects.nonNull(rule.getEndDate()) && timeDate.isAfter(rule.getEndDate())) {
            return Boolean.FALSE;
        }

        return Objects.isNull(rule.getMaxExecutions()) || rule.getExecutedCount() < rule.getMaxExecutions();
    }

    /**
     * 创建定时任务
     */
    public void scheduleJob(FortuneRecurringBillRuleModel rule) {
        try {
            String jobName = JOB_NAME_PREFIX + rule.getRuleId();

            // 删除已存在的任务
            JobKey jobKey = new JobKey(jobName, GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            // 创建JobDetail
            JobDetail jobDetail = JobBuilder.newJob(FortuneRecurringBillJob.class)
                    .withIdentity(jobName, GROUP_NAME)
                    .usingJobData("ruleId", rule.getRuleId())
                    .build();

            // 创建Trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "_Trigger", GROUP_NAME)
                    .withSchedule(CronScheduleBuilder.cronSchedule(rule.getCronExpression()))
                    .build();

            // 调度任务
            scheduler.scheduleJob(jobDetail, trigger);

            // 更新下次执行时间
            Date nextFireTime = trigger.getNextFireTime();
            if (Objects.nonNull(nextFireTime)) {
                this.updateNextExecutionTime(
                        rule.getRuleId(),
                        nextFireTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                );
            }

        } catch (Exception e) {
            log.error("创建周期记账任务失败，规则ID: {}", rule.getRuleId(), e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_CREATE_JOB_FAILED, rule.getRuleId());
        }
    }

    /**
     * 执行周期记账
     */
    public void executeRecurringBill(Long ruleId) {
        this.executeRecurringBillWithTime(ruleId, LocalDateTime.now());
    }

    /**
     * 执行周期记账（指定执行时间）
     */
    public void executeRecurringBillWithTime(Long ruleId, LocalDateTime executionTime) {
        long startTime = System.currentTimeMillis();
        try {
            FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
            if (!rule.getEnable()) {
                log.warn("规则已禁用，规则ID: {}", ruleId);
                return;
            }

            // 检查是否超过最大执行次数
            if (rule.checkOverExecutions()) {
                log.info("规则已达到最大执行次数，停止执行，规则ID: {}", ruleId);
                this.disableRule(ruleId);
                return;
            }

            // 检查是否超过结束日期
            if (rule.checkOverEndDate(executionTime.toLocalDate())) {
                log.info("规则已超过结束日期，停止执行，规则ID: {}", ruleId);
                this.disableRule(ruleId);
                return;
            }

            // 解析账单参数并执行
            String billRequestJson = rule.getBillRequest();
            FortuneBillAddCommand billRequest = JacksonUtil.from(billRequestJson, FortuneBillAddCommand.class);
            // 设置交易时间
            billRequest.setTradeTime(executionTime);
            // 执行记账逻辑
            Long billId = fortuneBillService.add(billRequest);

            // 更新执行记录
            this.updateExecutionRecord(ruleId, executionTime);
            this.recordSuccessLog(ruleId, executionTime, billId, System.currentTimeMillis() - startTime);
            log.info("周期记账执行成功，规则ID: {}, 执行时间: {}", ruleId, executionTime);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            // 记录失败日志
            this.recordFailureLog(ruleId, executionTime, errorMsg, System.currentTimeMillis() - startTime);
            log.error("周期记账执行失败，规则ID: {}, 执行时间: {}, 错误信息: {}", ruleId, executionTime, errorMsg, e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_EXECUTION_FAILED, ruleId, errorMsg);
        }
    }

    /**
     * 更新执行记录
     */
    private void updateExecutionRecord(Long ruleId, LocalDateTime executionTime) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.setExecutedCount(rule.getExecutedCount() + 1);
        rule.setLastExecutedTime(executionTime);
        // 计算下次执行时间
        try {
            CronExpression cronExpr = new CronExpression(rule.getCronExpression());
            Date nextTime = cronExpr.getNextValidTimeAfter(new Date());
            if (Objects.nonNull(nextTime)) {
                rule.setNextExecutionTime(nextTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        } catch (ParseException e) {
            log.error("计算下次执行时间失败", e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_CALCULATE_NEXT_EXECUTION_TIME_FAILED, ruleId);
        }
        rule.updateById();
    }

    /**
     * 更新补偿检查时间
     */
    private void updateRecoveryCheckTime(Long ruleId, LocalDateTime checkTime) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.setLastRecoveryCheck(checkTime);
        rule.updateById();
    }

    /**
     * 更新下次执行时间
     */
    private void updateNextExecutionTime(Long ruleId, LocalDateTime nextTime) {
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.setNextExecutionTime(nextTime);
        rule.updateById();
    }

    /**
     * 禁用规则重载
     */
    private void disableRule(Long ruleId) {
        this.deleteJob(ruleId);
        FortuneRecurringBillRuleModel rule = fortuneRecurringBillRuleFactory.loadById(ruleId);
        rule.setEnable(Boolean.FALSE);
        rule.updateById();
    }

    private void deleteJob(Long ruleId) {
        try {
            String jobName = JOB_NAME_PREFIX + ruleId;
            JobKey jobKey = new JobKey(jobName, GROUP_NAME);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("删除定时任务失败，规则ID: {}", ruleId, e);
            throw new ApiException(ErrorCode.Business.RECURRING_BILL_REMOVE_JOB_FAILED, ruleId);
        }
    }

    /**
     * 记录成功日志
     */
    private void recordSuccessLog(Long ruleId, LocalDateTime executionTime, Long billId, Long duration) {
        this.recordExecutionLog(ruleId, executionTime, RecurringBillLogStatusEnum.SUCCESS.getValue(), billId, null, duration);
    }

    /**
     * 记录失败日志
     */
    private void recordFailureLog(Long ruleId, LocalDateTime executionTime, String errorMsg, Long duration) {
        this.recordExecutionLog(ruleId, executionTime, RecurringBillLogStatusEnum.FAILURE.getValue(), null, errorMsg, duration);
    }

    /**
     * 记录执行日志
     */
    private void recordExecutionLog(Long ruleId, LocalDateTime executionTime, Integer status, Long billId, String errorMsg, Long duration) {
        FortuneRecurringBillLogEntity logEntity = new FortuneRecurringBillLogEntity();
        logEntity.setRuleId(ruleId);
        logEntity.setExecutionTime(executionTime);
        logEntity.setStatus(status);
        logEntity.setBillId(billId);
        logEntity.setErrorMsg(errorMsg);
        logEntity.setExecutionDuration(duration);

        fortuneRecurringBillLogRepository.save(logEntity);
    }

    public Boolean checkCronExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }
}