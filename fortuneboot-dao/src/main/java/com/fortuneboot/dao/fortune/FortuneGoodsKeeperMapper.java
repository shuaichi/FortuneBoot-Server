package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;

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
}