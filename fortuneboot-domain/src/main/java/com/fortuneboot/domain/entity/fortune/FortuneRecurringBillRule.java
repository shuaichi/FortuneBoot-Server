package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期记账规则表
 *
 * @author zhangchi118
 * @date 2025/6/30 19:51
 **/
@Data
@TableName("fortune_recurring_bill_rule")
@EqualsAndHashCode(callSuper = true)
public class FortuneRecurringBillRule extends BaseEntity<FortuneRecurringBillRule> {

    /**
     * 主键
     */
    @Schema(description = "主键")
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;

    /**
     * 账本id
     */
    @Schema(description = "账本ID")
    @TableField("book_id")
    private Long bookId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    @TableField("role_name")
    private String ruleName;

    /**
     * cron表达式
     */
    @Schema(description = "cron表达式")
    @TableField("cron_expression")
    private String cronExpression;

    /**
     * 启用状态
     */
    @Schema(description = "启用状态")
    @TableField("enable")
    private Boolean enable;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    @TableField("bill_request")
    private String billRequest;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 最大执行次数
     */
    @Schema(description = "max_executions")
    @TableField("最大执行次数")
    private Long maxExecutions;

    /**
     * 已经执行次数
     */
    @Schema(description = "已经执行次数")
    @TableField("executed_count")
    private Long executedCount;

    /**
     * 最后一次执行时间
     */
    @Schema(description = "最后一次执行时间")
    @TableField("last_executed_time")
    private LocalDateTime lastExecutedTime;

    /**
     * 下次执行时间
     */
    @Schema(description = "下次执行时间")
    @TableField("next_execution_time")
    private LocalDateTime nextExecutionTime;

    /**
     * 上次补偿时间
     */
    @Schema(description = "上次补偿时间")
    @TableField("last_recovery_check")
    private LocalDateTime lastRecoveryCheck;

    /**
     * 补偿策略
     */
    @Schema(description = "补偿策略")
    @TableField("recovery_strategy")
    private Integer recoveryStrategy;

    /**
     * 最大补偿次数
     */
    @Schema(description = "最大补偿次数")
    @TableField("max_recovery_count")
    private Long maxRecoveryCount;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

}
