package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.factory.fortune.model.FortuneGoodsKeeperModel;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 11:17
 **/
@Component
@RequiredArgsConstructor
public class FortuneGoodsKeeperFactory {

    private final FortuneGoodsKeeperRepository fortuneGoodsKeeperRepository;

    public FortuneGoodsKeeperModel create(){
        return new FortuneGoodsKeeperModel(fortuneGoodsKeeperRepository);
    }

    public FortuneGoodsKeeperModel loadById(Long goodsKeeperId){
        FortuneGoodsKeeperEntity entity = fortuneGoodsKeeperRepository.getById(goodsKeeperId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, goodsKeeperId, "物品");
        }
        return new FortuneGoodsKeeperModel(entity,fortuneGoodsKeeperRepository);
    }
}
