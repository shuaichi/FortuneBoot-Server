package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 23:34
 **/
@Component
@RequiredArgsConstructor
public class FortuneAccountFactory {

    private final FortuneAccountRepository fortuneAccountRepository;

    public FortuneAccountModel create() {
        return new FortuneAccountModel(fortuneAccountRepository);
    }

    public FortuneAccountModel loadById(Long accountId){
        FortuneAccountEntity fortuneAccountEntity = fortuneAccountRepository.getById(accountId);
        if (Objects.isNull(fortuneAccountEntity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, accountId, "账户");
        }
        return new FortuneAccountModel(fortuneAccountEntity,fortuneAccountRepository);
    }
}
