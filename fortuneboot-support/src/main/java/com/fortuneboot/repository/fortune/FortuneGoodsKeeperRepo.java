package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;

import java.math.BigDecimal;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 10:57
 **/
public interface FortuneGoodsKeeperRepo extends IService<FortuneGoodsKeeperEntity> {

    /**
     * 查询归物的统计
     *
     * @param bookId
     * @return
     */
    FortuneGoodsKeeperStatisticsVo getGoodsKeeperStatistics(Long bookId);

    /**
     * 关联账单自动叠加的净成本（支出加/收入减）
     *
     * @param goodsKeeperId 归物id
     * @return 净成本，未关联任何账单时为 0
     */
    BigDecimal sumLinkedBillAmount(Long goodsKeeperId);
}
