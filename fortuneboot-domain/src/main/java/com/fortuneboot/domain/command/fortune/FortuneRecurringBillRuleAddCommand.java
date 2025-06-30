package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期记账规则Add类
 *
 * @author zhangchi118
 * @date 2025/6/30 20:55
 **/
@Data
public class FortuneRecurringBillRuleAddCommand {

    /**
     * 账本id
     */
    @NotNull(message = "账本id不能为空")
    @Positive(message = "账本必须是正整数")
    private Long bookId;

    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称不能超过100个字符")
    private String ruleName;

    /**
     * cron表达式
     */
    @NotBlank(message = "cron表达式不能为空")
    private String cronExpression;

    /**
     * 启用状态
     */
    private Boolean enable;

    /**
     * 请求参数
     */
    @NotNull(message = "账单数据不能为空")
    private FortuneBillAddCommand billRequest;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 最大执行次数
     */
    private Long maxExecutions;

    /**
     * 已经执行次数
     */
    private Long executedCount;

    /**
     * 最后一次执行时间
     */
    private LocalDateTime lastExecutedTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecutionTime;

    /**
     * 上次补偿时间
     */
    private LocalDateTime lastRecoveryCheck;

    /**
     * 补偿策略
     */
    private Integer recoveryStrategy;

    /**
     * 最大补偿次数
     */
    private Long maxRecoveryCount;

    /**
     * 备注
     */
    @Size(max = 1024, message = "备注长度不能超过1024个字符")
    private String remark;
}
