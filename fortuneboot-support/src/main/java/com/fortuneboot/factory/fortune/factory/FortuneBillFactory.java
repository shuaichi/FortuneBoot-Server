package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:43
 **/
@Component
@RequiredArgsConstructor
public class FortuneBillFactory {

    private final FortuneBillRepo fortuneBillRepo;

    public FortuneBillModel create() {
        return new FortuneBillModel(fortuneBillRepo);
    }

    public FortuneBillModel loadById(Long billId) {
        FortuneBillEntity entity = fortuneBillRepo.getById(billId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, billId, "账单");
        }
        return new FortuneBillModel(entity, fortuneBillRepo);
    }
}
