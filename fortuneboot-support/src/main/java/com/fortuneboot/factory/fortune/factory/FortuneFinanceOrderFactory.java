package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.factory.fortune.model.FortuneFinanceOrderModel;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 单据表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:14
 **/
@Component
@RequiredArgsConstructor
public class FortuneFinanceOrderFactory {

    private final FortuneFinanceOrderRepo fortuneFinanceOrderRepo;

    public FortuneFinanceOrderModel create(){
        return new FortuneFinanceOrderModel(fortuneFinanceOrderRepo);
    }

    public FortuneFinanceOrderModel loadById(Long orderId){
        FortuneFinanceOrderEntity entity = fortuneFinanceOrderRepo.getById(orderId);
        if(Objects.isNull(entity)){
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, orderId, "单据");
        }
        return new FortuneFinanceOrderModel(fortuneFinanceOrderRepo,entity);
    }
}
