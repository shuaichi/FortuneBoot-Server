package com.fortuneboot.domain.vo.fortune;

import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/7/4 19:40
 **/
@Data
public class FortuneRecurringBillRuleVo {

    public FortuneRecurringBillRuleVo(FortuneRecurringBillRuleEntity entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 启用状态
     */
    private Boolean enable;

    /**
     * 请求参数
     */
    private String billRequest;

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
    private String remark;
}
