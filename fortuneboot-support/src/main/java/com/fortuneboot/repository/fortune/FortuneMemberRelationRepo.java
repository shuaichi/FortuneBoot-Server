package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;

import java.util.List;
import java.util.Map;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:31
 **/
public interface FortuneMemberRelationRepo extends IService<FortuneMemberRelationEntity> {
    List<FortuneMemberRelationEntity> getByBillId(Long billId);

    Map<Long, List<FortuneMemberRelationEntity>> getByBillIdList(List<Long> billIdList);

    void removeByBillId(Long billId);

    void phyRemoveByBillId(Long billId);

    void removeByBillIds(List<Long> billIds);

    Boolean existByMemberId(Long memberId);
}