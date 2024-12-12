package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

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
    private Long groupId;

    /**
     * 账本名称
     */
    private String bookName;

    /**
     * 是否回收站(必传)
     */
    private Boolean recycleBin;

    @Override
    public LambdaQueryWrapper<FortuneBookEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneBookEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBookEntity.class);
        queryWrapper.eq(FortuneBookEntity::getGroupId, groupId)
                .eq(FortuneBookEntity::getRecycleBin, recycleBin)
                .eq(StringUtils.isNotEmpty(bookName), FortuneBookEntity::getBookName, bookName);
        return queryWrapper;
    }
}
