package com.fortuneboot.customize.recurringBillLog;

import com.fortuneboot.common.enums.fortune.RecurringBillLogStatusEnum;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import com.fortuneboot.repository.fortune.FortuneRecurringBillLogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/7/10 09:30
 **/
@Slf4j
@Aspect
@Component
@Order(1)
@AllArgsConstructor
public class RecurringBillLogAspect {

    private final FortuneRecurringBillLogRepository fortuneRecurringBillLogRepository;

    @Around("@annotation(recurringBillLog)")
    public Object around(ProceedingJoinPoint joinPoint, RecurringBillLog recurringBillLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime executionTime = LocalDateTime.now();

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        Long ruleId = this.extractRuleId(args);

        String logDescription = recurringBillLog.value();

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录成功日志
            Long billId = this.extractBillId(result);
            Long duration = System.currentTimeMillis() - startTime;

            this.recordExecutionLog(ruleId, executionTime, RecurringBillLogStatusEnum.SUCCESS.getValue(),
                    billId, null, duration);

            log.info("{}成功，规则ID: {}, 执行时间: {}, 耗时: {}ms",
                    logDescription, ruleId, executionTime, duration);

            return result;

        } catch (Exception e) {
            // 记录失败日志
            Long duration = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage();

            this.recordExecutionLog(ruleId, executionTime, RecurringBillLogStatusEnum.FAILURE.getValue(),
                    null, errorMsg, duration);

            log.error("{}失败，规则ID: {}, 执行时间: {}, 错误信息: {}, 耗时: {}ms",
                    logDescription, ruleId, executionTime, errorMsg, duration, e);

            throw e;
        }
    }

    /**
     * 从参数中提取规则ID
     */
    private Long extractRuleId(Object[] args) {
        if (Objects.nonNull(args)  && args.length > 0 && args[0] instanceof Long) {
            return (Long) args[0];
        }
        return null;
    }

    /**
     * 从返回值中提取账单ID
     */
    private Long extractBillId(Object result) {
        return switch (result) {
            case Long billId -> billId;
            case Number billId -> billId.longValue();
            case null, default -> null;
        };
    }

    /**
     * 记录执行日志
     */
    private void recordExecutionLog(Long ruleId, LocalDateTime executionTime, Integer status,
                                    Long billId, String errorMsg, Long duration) {
        if (Objects.isNull(ruleId) || Objects.isNull(billId)) {
            log.warn("规则ID或者账单ID为空，无法记录执行日志");
            return;
        }

        try {
            FortuneRecurringBillLogEntity logEntity = new FortuneRecurringBillLogEntity();
            logEntity.setRuleId(ruleId);
            logEntity.setExecutionTime(executionTime);
            logEntity.setStatus(status);
            logEntity.setBillId(billId);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setExecutionDuration(duration);

            fortuneRecurringBillLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("记录周期记账执行日志失败", e);
        }
    }
}