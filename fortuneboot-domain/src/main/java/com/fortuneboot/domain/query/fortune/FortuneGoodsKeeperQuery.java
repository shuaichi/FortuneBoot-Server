package com.fortuneboot.domain.query.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fortuneboot.common.core.page.AbstractLambdaPageQuery;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangchi118
 * @date 2025/5/6 14:39
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneGoodsKeeperQuery extends AbstractLambdaPageQuery<FortuneGoodsKeeperEntity> {

    /**
     * 账本id(必传)
     */
    @NotNull
    @Positive
    private Long bookId;

    private String goodsName;


    @Override
    public LambdaQueryWrapper<FortuneGoodsKeeperEntity> addQueryCondition() {
        LambdaQueryWrapper<FortuneGoodsKeeperEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneGoodsKeeperEntity.class);
        queryWrapper.eq(FortuneGoodsKeeperEntity::getBookId, bookId)
                .like(StringUtils.isNotBlank(goodsName), FortuneGoodsKeeperEntity::getGoodsName, goodsName)
                .orderByDesc(FortuneGoodsKeeperEntity::getPurchaseDate);
        return queryWrapper;
    }
}
