package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/1/10 17:21
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortunePayeeQuery extends AbstractLambdaPageQuery<FortunePayeeEntity> {

    /**
     * 账本id(必传)
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 是否回收站(必传)
     */
    @NotNull
    private Boolean recycleBin;

    /**
     * 交易对象名
     */
    private String payeeName;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;


    @Override
    public LambdaQueryWrapper<FortunePayeeEntity> addQueryCondition() {
        LambdaQueryWrapper<FortunePayeeEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortunePayeeEntity.class);
        queryWrapper.eq(FortunePayeeEntity::getBookId, bookId)
                .eq( FortunePayeeEntity::getRecycleBin, recycleBin)
                .eq(Objects.nonNull(enable), FortunePayeeEntity::getEnable, enable)
                .eq(Objects.nonNull(canExpense), FortunePayeeEntity::getCanExpense, canExpense)
                .eq(Objects.nonNull(canIncome), FortunePayeeEntity::getCanIncome, canIncome)
                .like(StringUtils.isNotBlank(payeeName), FortunePayeeEntity::getPayeeName, payeeName)
                .orderByAsc(FortunePayeeEntity::getSort);
        return queryWrapper;
    }
}
