package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 账本工厂
 * @author zhangchi118
 * @date 2024/11/29 16:09
 **/
@Component
@RequiredArgsConstructor
public class FortuneBookFactory {

    private final FortuneBookRepository fortuneBookRepository;


    public FortuneBookModel loadById(Long bookId) {
        FortuneBookEntity fortuneBookEntity = fortuneBookRepository.getById(bookId);
        if (Objects.isNull(fortuneBookEntity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, bookId, "账本");
        }
        return new FortuneBookModel(fortuneBookEntity,fortuneBookRepository);
    }

    public FortuneBookModel create() {
        return new FortuneBookModel(fortuneBookRepository);
    }
}
