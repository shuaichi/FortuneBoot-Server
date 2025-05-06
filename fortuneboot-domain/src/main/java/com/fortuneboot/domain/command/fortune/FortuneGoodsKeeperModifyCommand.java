package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 归物修改
 *
 * @author zhangchi118
 * @date 2025/5/6 11:10
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneGoodsKeeperModifyCommand extends FortuneGoodsKeeperAddCommand {

    /**
     * 物品id
     */
    @NotNull
    @Positive
    private Long goodsKeeperId;
}
