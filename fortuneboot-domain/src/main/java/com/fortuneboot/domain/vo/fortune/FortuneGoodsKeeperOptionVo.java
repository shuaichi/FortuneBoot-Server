package com.fortuneboot.domain.vo.fortune;

import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 归物下拉选项
 *
 * @author fortuneboot
 * @date 2026/10/23
 **/
@Data
public class FortuneGoodsKeeperOptionVo {

    private Long goodsKeeperId;

    private String goodsName;

    private BigDecimal price;

    private BigDecimal accumulatedCost;

    public FortuneGoodsKeeperOptionVo(FortuneGoodsKeeperEntity entity) {
        if (entity != null) {
            this.goodsKeeperId = entity.getGoodsKeeperId();
            this.goodsName = entity.getGoodsName();
            this.price = entity.getPrice();
            this.accumulatedCost = entity.getAccumulatedCost();
        }
    }
}
