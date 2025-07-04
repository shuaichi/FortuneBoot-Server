package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/7/4 19:50
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneRecurringBillRuleQuery extends AbstractLambdaPageQuery<FortuneRecurringBillRuleEntity> {

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
     * 备注
     */
    private String remark;

    @Override
    public LambdaQueryWrapper<FortuneRecurringBillRuleEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneRecurringBillRuleEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneRecurringBillRuleEntity.class);
        queryWrapper.eq(FortuneRecurringBillRuleEntity::getBookId, bookId)
                .like(StringUtils.isNotBlank(ruleName), FortuneRecurringBillRuleEntity::getRuleName, ruleName)
                .like(StringUtils.isNotBlank(cronExpression), FortuneRecurringBillRuleEntity::getCronExpression, cronExpression)
                .eq(Objects.nonNull(enable), FortuneRecurringBillRuleEntity::getEnable, enable)
                .like(StringUtils.isNotBlank(remark), FortuneRecurringBillRuleEntity::getRemark, remark);
        return queryWrapper;
    }
}
