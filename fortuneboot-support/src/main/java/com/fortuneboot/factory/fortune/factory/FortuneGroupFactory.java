package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import com.fortuneboot.factory.fortune.model.FortuneGroupModel;
import com.fortuneboot.repository.fortune.FortuneGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 分组模型工厂
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 18:37
 **/
@Component
@RequiredArgsConstructor
public class FortuneGroupFactory {

    private final FortuneGroupRepository fortuneGroupRepository;

    public FortuneGroupModel loadById(Long groupId) {
        FortuneGroupEntity fortuneGroupEntity = fortuneGroupRepository.getById(groupId);
        if (Objects.isNull(fortuneGroupEntity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, groupId, "分组");
        }
        return new FortuneGroupModel(fortuneGroupEntity, fortuneGroupRepository);
    }

    public FortuneGroupModel create() {
        return new FortuneGroupModel(fortuneGroupRepository);
    }
}
