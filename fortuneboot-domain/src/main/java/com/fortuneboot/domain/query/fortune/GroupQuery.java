package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/15 23:44
 **/
public class GroupQuery extends AbstractLambdaPageQuery<FortuneGroupEntity> {

    @Override
    public LambdaQueryWrapper<FortuneGroupEntity> addQueryCondition() {
        return WrapperUtil.getLambdaQueryWrapper(FortuneGroupEntity.class);
    }
}
