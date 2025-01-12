package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 23:04
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBillQuery extends AbstractLambdaPageQuery<FortuneBillEntity> {

    private String bookId;


    @Override
    public LambdaQueryWrapper<FortuneBillEntity> addQueryCondition() {
        return null;
    }
}
