package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author zhangchi118
 * @date 2025/1/10 18:43
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryQuery extends AbstractLambdaPageQuery<FortuneCategoryEntity> {


    /**
     * 账本id(必传)
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 标签名
     */
    private String categoryName;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 分类类型
     * @see com.fortuneboot.common.enums.fortune.CategoryTypeEnum
     */
    private Integer categoryType;

    /**
     * 是否回收站(必传)
     */
    @NotNull
    private Boolean recycleBin;

    @Override
    public LambdaQueryWrapper<FortuneCategoryEntity> addQueryCondition() {
        return this.addQueryCondition(Boolean.FALSE);
    }

    public LambdaQueryWrapper<FortuneCategoryEntity> addQueryCondition(Boolean conditionQuery) {
        LambdaQueryWrapper<FortuneCategoryEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryEntity.class);
        queryWrapper.eq(FortuneCategoryEntity::getBookId, bookId)
                .eq( FortuneCategoryEntity::getRecycleBin, recycleBin)
                .eq(Objects.nonNull(categoryType), FortuneCategoryEntity::getCategoryType, categoryType)
                .eq(Objects.nonNull(enable), FortuneCategoryEntity::getEnable, enable)
                .orderByAsc(FortuneCategoryEntity::getSort);
        if (conditionQuery){
            queryWrapper.like(StringUtils.isNotBlank(categoryName), FortuneCategoryEntity::getCategoryName, categoryName);
        }
        return queryWrapper;
    }

    public Boolean conditionQuery() {
        return StringUtils.isNotBlank(categoryName);
    }
}
