package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:03
 **/
@Data
@TableName("fortune_recurring_bill_log")
@EqualsAndHashCode(callSuper = true)
public class FortuneRecurringBillLog extends BaseEntity<FortuneRecurringBillLog> {
    /**
     * 主键
     */
    @Schema(description = "主键")
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 规则ID
     */
    @Schema(description = "规则id")
    @TableField("rule_id")
    private Long ruleId;

    @Schema(description = "执行时间")
    @TableField("execution_time")
    private LocalDateTime executionTime;

    /**
     * 状态
     */
    @Schema(description = "状态")
    @TableField("status")
    private Integer status;

    /**
     * 账单ID
     */
    @Schema(description = "账单ID")
    @TableField("bill_id")
    private Long billId;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 执行耗时
     */
    @Schema(description = "执行耗时")
    @TableField("execution_duration")
    private Long executionDuration;
}
