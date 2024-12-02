package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分组查询
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/15 23:44
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneGroupQuery extends AbstractLambdaPageQuery<FortuneGroupEntity> {

    @Override
    public LambdaQueryWrapper<FortuneGroupEntity> addQueryCondition() {
        return WrapperUtil.getLambdaQueryWrapper(FortuneGroupEntity.class);
    }
}
