package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneGoodsKeeperMapper;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.domain.vo.fortune.FortuneGoodsKeeperStatisticsVo;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 10:58
 **/
@Service
@AllArgsConstructor
public class FortuneGoodsKeeperRepoImpl extends ServiceImpl<FortuneGoodsKeeperMapper, FortuneGoodsKeeperEntity> implements FortuneGoodsKeeperRepo {

    private final FortuneGoodsKeeperMapper fortuneGoodsKeeperMapper;

    @Override
    public FortuneGoodsKeeperStatisticsVo getGoodsKeeperStatistics(Long bookId) {
        return fortuneGoodsKeeperMapper.getGoodsKeeperStatistics(bookId);
    }
}
