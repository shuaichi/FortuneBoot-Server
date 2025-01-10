package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
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
    private Long bookId;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 是否回收站
     */
    private Boolean recycleBin;

    @Override
    public LambdaQueryWrapper<FortuneTagEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneTagEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagEntity.class);
        queryWrapper.eq(FortuneTagEntity::getBookId, bookId)
                .like(StringUtils.isNotBlank(tagName), FortuneTagEntity::getTagName, tagName)
                .eq(Objects.nonNull(enable), FortuneTagEntity::getEnable, enable)
                .eq(Objects.nonNull(recycleBin), FortuneTagEntity::getRecycleBin, recycleBin);
        return queryWrapper;
    }
}
