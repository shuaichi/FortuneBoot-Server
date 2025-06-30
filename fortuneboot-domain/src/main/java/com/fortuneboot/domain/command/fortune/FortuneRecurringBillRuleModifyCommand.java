package com.fortuneboot.domain.command.fortune;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 周期记账规则Modify类
 *
 * @author zhangchi118
 * @date 2025/6/30 20:58
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneRecurringBillRuleModifyCommand extends FortuneRecurringBillRuleAddCommand {

    /**
     * 规则id
     */
    private Long ruleId;

}
