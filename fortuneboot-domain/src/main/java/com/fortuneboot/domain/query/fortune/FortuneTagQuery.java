package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 账本标签查询条件
 *
 * @author zhangchi118
 * @date 2024/12/11 16:45
 **/
@Data
public class FortuneTagQuery extends AbstractLambdaPageQuery<FortuneTagEntity> {

    /**
     * 账本id(必传)
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 可转账
     */
    private Boolean canTransfer;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 是否回收站(必传)
     */
    @NotNull
    private Boolean recycleBin;

    @Override
    public LambdaQueryWrapper<FortuneTagEntity> addQueryCondition() {
        return this.addQueryCondition(Boolean.FALSE);
    }

    public LambdaQueryWrapper<FortuneTagEntity> addQueryCondition(Boolean conditionQuery) {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.eq(FortuneTagEntity::getBookId, bookId)
                .eq(FortuneTagEntity::getRecycleBin, recycleBin)
                .orderByAsc(FortuneTagEntity::getSort);
        if (conditionQuery) {
            queryWrapper.eq(Objects.nonNull(canExpense), FortuneTagEntity::getCanExpense, canExpense)
                    .eq(Objects.nonNull(canIncome), FortuneTagEntity::getCanIncome, canIncome)
                    .eq(Objects.nonNull(canTransfer), FortuneTagEntity::getCanTransfer, canTransfer)
                    .eq(Objects.nonNull(enable), FortuneTagEntity::getEnable, enable)
                    .like(StringUtils.isNotBlank(tagName), FortuneTagEntity::getTagName, tagName);
        }
        return queryWrapper;
    }

    public Boolean conditionQuery() {
        return StringUtils.isNotBlank(tagName) | Objects.nonNull(canExpense) | Objects.nonNull(canIncome) | Objects.nonNull(canTransfer) | Objects.nonNull(enable);
    }
}
