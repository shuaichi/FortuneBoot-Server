package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.factory.fortune.model.FortuneBillExtraModel;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 账单附加费用 Factory
 *
 * @author zhangchi118
 **/
@Component
@RequiredArgsConstructor
public class FortuneBillExtraFactory {

    private final FortuneBillExtraRepo fortuneBillExtraRepo;

    public FortuneBillExtraModel create() {
        return new FortuneBillExtraModel(fortuneBillExtraRepo);
    }

    public FortuneBillExtraModel loadById(Long extraId) {
        FortuneBillExtraEntity entity = fortuneBillExtraRepo.getById(extraId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, extraId, "账单附加费用");
        }
        return new FortuneBillExtraModel(entity, fortuneBillExtraRepo);
    }
}