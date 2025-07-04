package com.fortuneboot.domain.vo.fortune;

import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 周期记账执行日志VO
 *
 * @author zhangchi118
 * @date 2025/7/4 20:03
 **/
@Data
public class FortuneRecurringBillLogVo {
    public FortuneRecurringBillLogVo(FortuneRecurringBillLogEntity entity) {
        if (Objects.nonNull(entity)) {
            BeanUtils.copyProperties(entity, this);
        }
    }

    /**
     * 日志id
     */
    private Long logId;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 执行时间
     */
    private LocalDateTime executionTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 账单ID
     */
    private Long billId;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 执行耗时
     */
    private Long executionDuration;

}
