package com.fortuneboot.service.fortune.recurringBill;

import com.fortuneboot.common.enums.fortune.RecoveryStrategyEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.factory.fortune.model.FortuneRecurringBillRuleModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 周期记账补偿服务
 *
 * @author zhangchi118
 * @date 2025/7/10 14:47
 **/
@Slf4j
@Service
@AllArgsConstructor
public class FortuneRecurringBillRecoveryService {

    private final FortuneRecurringBillExecutor fortuneRecurringBillExecutor;

    /**
     * 执行补偿逻辑
     */
    public void performRecovery(FortuneRecurringBillRuleModel rule) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastCheck = rule.getLastRecoveryCheck();

        if (Objects.isNull(lastCheck)) {
            lastCheck = rule.getCreateTime();
        }

        // 以最后一次真实执行时间为基准，避免重复补偿
        LocalDateTime baseline = lastCheck;
        LocalDateTime lastExecuted = rule.getLastExecutedTime();
        if (Objects.nonNull(lastExecuted) && lastExecuted.isAfter(baseline)) {
            baseline = lastExecuted;
        }

        RecoveryStrategyEnum strategy = RecoveryStrategyEnum.getEnumByValue(rule.getRecoveryStrategy());
        if (Objects.equals(strategy, RecoveryStrategyEnum.NO_RECOVERY)) {
            return;
        }

        // 如果基准时间不早于当前时间，则无需补偿
        if (!baseline.isBefore(now)) {
            return;
        }

        // 计算应该执行但未执行的时间点
        List<LocalDateTime> missedExecutions = calculateMissedExecutions(rule, baseline, now);

        if (missedExecutions.isEmpty()) {
            return;
        }

        // 根据策略过滤需要补偿的执行点
        List<LocalDateTime> toRecover = filterRecoveryExecutions(missedExecutions, strategy, rule.getMaxRecoveryCount());

        log.info("规则[{}]需要补偿{}次执行", rule.getRuleName(), toRecover.size());

        // 执行补偿
        for (LocalDateTime executionTime : toRecover) {
            fortuneRecurringBillExecutor.executeRecurringBill(rule.getRuleId(), executionTime);
        }
    }

    /**
     * 计算错过的执行时间点
     */
    private List<LocalDateTime> calculateMissedExecutions(FortuneRecurringBillRuleModel rule, LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> missed = new ArrayList<>();

        try {
            CronExpression cronExpr = new CronExpression(rule.getCronExpression());

            // 确保from不早于startDate
            LocalDateTime effectiveFrom = from;
            if (Objects.nonNull(rule.getStartDate())) {
                LocalDateTime startDateTime = rule.getStartDate().atStartOfDay();
                if (effectiveFrom.isBefore(startDateTime)) {
                    effectiveFrom = startDateTime;
                }
            }

            // 确保to不晚于endDate
            LocalDateTime effectiveTo = to;
            if (Objects.nonNull(rule.getEndDate())) {
                LocalDateTime endDateTime = rule.getEndDate().atTime(23, 59, 59);
                if (effectiveTo.isAfter(endDateTime)) {
                    effectiveTo = endDateTime;
                }
            }

            LocalDateTime current = effectiveFrom;
            while (current.isBefore(effectiveTo)) {
                Date nextTime = cronExpr.getNextValidTimeAfter(Date.from(current.atZone(ZoneId.systemDefault()).toInstant()));
                if (Objects.isNull(nextTime)) {
                    break;
                }

                LocalDateTime nextExecution = nextTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                if (nextExecution.isAfter(effectiveTo)) {
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
}