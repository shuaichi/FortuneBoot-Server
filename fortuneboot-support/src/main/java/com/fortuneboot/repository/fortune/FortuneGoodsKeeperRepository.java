package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 10:57
 **/
public interface FortuneGoodsKeeperRepository extends IService<FortuneGoodsKeeperEntity> {

    /**
     * 查询归物的统计
     *
     * @param bookId
     * @return
     */
    FortuneGoodsKeeperStatisticsVo getGoodsKeeperStatistics(Long bookId);
}
