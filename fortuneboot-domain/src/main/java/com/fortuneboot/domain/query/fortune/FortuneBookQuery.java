package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 账本查询
 *
 * @author zhangchi118
 * @date 2024/12/2 16:00
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBookQuery extends AbstractLambdaPageQuery<FortuneBookEntity> {

    /**
     * 分组ID(必传)
     */
    @NotNull
    @Positive
    private Long groupId;

    /**
     * 账本名称
     */
    private String bookName;

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
    public LambdaQueryWrapper<FortuneBookEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneBookEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBookEntity.class);
        queryWrapper.eq(FortuneBookEntity::getGroupId, groupId)
                .eq(FortuneBookEntity::getRecycleBin, recycleBin)
                .eq(Objects.nonNull(enable), FortuneBookEntity::getEnable, enable)
                .like(StringUtils.isNotEmpty(bookName), FortuneBookEntity::getBookName, bookName)
                .orderByAsc(FortuneBookEntity::getSort);
        return queryWrapper;
    }
}
