package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 账本标签工厂
 *
 * @author zhangchi118
 * @date 2024/12/11 16:14
 **/
@Component
@RequiredArgsConstructor
public class FortuneTagFactory {

    private final FortuneTagRepository fortuneTagRepository;

    public FortuneTagModel loadById(Long tagId) {
        FortuneTagEntity entity = fortuneTagRepository.getById(tagId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, tagId, "标签");
        }
        return new FortuneTagModel(entity, fortuneTagRepository);
    }

    public FortuneTagModel create() {
        return new FortuneTagModel(fortuneTagRepository);
    }
}
