package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.factory.fortune.model.FortunePayeeModel;
import com.fortuneboot.repository.fortune.FortunePayeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 交易对象工厂
 *
 * @author zhangchi118
 * @date 2024/12/11 16:14
 **/
@Component
@RequiredArgsConstructor
public class FortunePayeeFactory {

    private final FortunePayeeRepository fortunePayeeRepository;

    public FortunePayeeModel loadById(Long payeeId) {
        FortunePayeeEntity entity = fortunePayeeRepository.getById(payeeId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, payeeId, "交易对象");
        }
        return new FortunePayeeModel(entity, fortunePayeeRepository);
    }

    public FortunePayeeModel create() {
        return new FortunePayeeModel(fortunePayeeRepository);
    }
}
