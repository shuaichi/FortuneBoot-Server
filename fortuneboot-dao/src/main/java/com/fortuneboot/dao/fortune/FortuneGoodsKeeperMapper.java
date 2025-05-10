package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;
import org.apache.ibatis.annotations.Select;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 10:53
 **/
public interface FortuneGoodsKeeperMapper extends BaseMapper<FortuneGoodsKeeperEntity> {

    @Select("""
            SELECT
                -- 全部物品成本(allPrice): 所有物品价格总和
                ROUND(SUM(price), 2) AS allPrice,
            
                -- 日均成本(allDailyPrice): 所有物品的日均成本总和
                ROUND(SUM(
                    CASE
                        -- 按次使用且有使用次数的情况
                        WHEN use_by_times = 1 AND usage_num > 0 THEN
                            (price - COALESCE(sold_price, 0)) / usage_num
                        -- 按次使用但使用次数为0或NULL的情况，直接使用价格作为日均成本
                        WHEN use_by_times = 1 AND (usage_num = 0 OR usage_num IS NULL) THEN
                            price - COALESCE(sold_price, 0)
                        -- 非按次使用的情况，使用修正后的天数计算
                        WHEN use_by_times = 0 THEN
                            (price - COALESCE(sold_price, 0)) /
                            (DATEDIFF(CURDATE(), purchase_date) + 1)
                        ELSE 0
                    END
                ), 2) AS allDailyPrice,
            
                -- 在役物品成本(activePrice): 状态为1的物品价格总和
                ROUND(SUM(CASE WHEN status = 1 THEN price ELSE 0 END), 2) AS activePrice,
            
                -- 在役物品日均成本(activeDailyPrice): 状态为1的物品日均成本总和
                ROUND(SUM(
                    CASE
                        -- 在役且按次使用且有使用次数的情况
                        WHEN status = 1 AND use_by_times = 1 AND usage_num > 0 THEN
                            (price - COALESCE(sold_price, 0)) / usage_num
                        -- 在役且按次使用但使用次数为0或NULL的情况
                        WHEN status = 1 AND use_by_times = 1 AND (usage_num = 0 OR usage_num IS NULL) THEN
                            price - COALESCE(sold_price, 0)
                        -- 在役且非按次使用的情况，使用修正后的天数计算
                        WHEN status = 1 AND use_by_times = 0 THEN
                            (price - COALESCE(sold_price, 0)) /
                            (DATEDIFF(CURDATE(), purchase_date) + 1)
                        ELSE 0
                    END
                ), 2) AS activeDailyPrice
            FROM
                fortune_goods_keeper
            WHERE
                book_id = #{bookId}
                AND deleted = 0;
            """)
    FortuneGoodsKeeperStatisticsVo getGoodsKeeperStatistics(Long bookId);
}
