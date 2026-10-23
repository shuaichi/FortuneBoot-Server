package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 10:53
 **/
public interface FortuneGoodsKeeperMapper extends BaseMapper<FortuneGoodsKeeperEntity> {

    /**
     * 归物统计 - 使用 XML 中的 databaseId 分发
     */
    FortuneGoodsKeeperStatisticsVo getGoodsKeeperStatistics(Long bookId);

    /**
     * 关联账单自动叠加的净成本：
     * 支出账单(amount, bill_type=1)累加，收入账单(amount, bill_type=2)冲减。
     * 仅统计未删除、未进回收站的流水。
     */
    BigDecimal sumLinkedBillAmount(@Param("goodsKeeperId") Long goodsKeeperId);
}