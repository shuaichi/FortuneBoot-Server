package com.fortuneboot.factory.fortune;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public List<FortuneTagModel> loadByIds(List<Long> tagIds) {
        List<FortuneTagEntity> entities = fortuneTagRepository.getByIds(tagIds);
        if (CollectionUtils.size(entities) != CollectionUtils.size(tagIds)) {
            List<Long> list = entities.stream().map(FortuneTagEntity::getTagId).toList();
            Collection<Long> subtracts = CollectionUtils.subtract(tagIds, list);
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, subtracts.toString(), "标签");
        }
        return entities.stream().map(item -> new FortuneTagModel(item, fortuneTagRepository)).toList();
    }

    public FortuneTagModel create() {
        return new FortuneTagModel(fortuneTagRepository);
    }
}
