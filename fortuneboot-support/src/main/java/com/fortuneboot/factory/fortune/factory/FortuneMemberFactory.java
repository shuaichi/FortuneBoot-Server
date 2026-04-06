package com.fortuneboot.factory.fortune.factory;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.factory.fortune.model.FortuneMemberModel;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:31
 **/

@Component
@RequiredArgsConstructor
public class FortuneMemberFactory {
    private final FortuneMemberRepo fortuneMemberRepo;

    public FortuneMemberModel loadById(Long memberId) {
        FortuneMemberEntity entity = fortuneMemberRepo.getById(memberId);
        if (Objects.isNull(entity)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, memberId, "成员");
        }
        return new FortuneMemberModel(entity, fortuneMemberRepo);
    }

    public List<FortuneMemberModel> loadByIds(List<Long> memberIds) {
        List<FortuneMemberEntity> entities = fortuneMemberRepo.getByIds(memberIds);
        if (CollectionUtils.size(entities) != CollectionUtils.size(memberIds)) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, "部分成员不存在", "成员");
        }
        return entities.stream().map(item -> new FortuneMemberModel(item, fortuneMemberRepo)).toList();
    }

    public FortuneMemberModel create() {
        return new FortuneMemberModel(fortuneMemberRepo);
    }
}