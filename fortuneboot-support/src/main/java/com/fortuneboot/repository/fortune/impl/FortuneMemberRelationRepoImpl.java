package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneMemberRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:31
 **/

@Service
@AllArgsConstructor
public class FortuneMemberRelationRepoImpl extends ServiceImpl<FortuneMemberRelationMapper, FortuneMemberRelationEntity> implements FortuneMemberRelationRepo {

    private final FortuneMemberRelationMapper fortuneMemberRelationMapper;

    @Override
    public List<FortuneMemberRelationEntity> getByBillId(Long billId) {
        LambdaQueryWrapper<FortuneMemberRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberRelationEntity.class);
        wrapper.eq(FortuneMemberRelationEntity::getBillId, billId);
        return this.list(wrapper);
    }

    @Override
    public Map<Long, List<FortuneMemberRelationEntity>> getByBillIdList(List<Long> billIdList) {
        LambdaQueryWrapper<FortuneMemberRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberRelationEntity.class);
        wrapper.in(FortuneMemberRelationEntity::getBillId, billIdList);
        return this.list(wrapper).stream().collect(Collectors.groupingBy(FortuneMemberRelationEntity::getBillId));
    }

    @Override
    public void removeByBillId(Long billId) {
        if (Objects.isNull(billId)) return;
        List<FortuneMemberRelationEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isNotEmpty(list)) {
            fortuneMemberRelationMapper.deleteByIds(list.stream().map(FortuneMemberRelationEntity::getMemberRelationId).toList());
        }
    }

    @Override
    public void phyRemoveByBillId(Long billId) {
        if (Objects.isNull(billId)) return;
        List<FortuneMemberRelationEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isNotEmpty(list)) {
            fortuneMemberRelationMapper.phyDeleteByIds(list.stream().map(FortuneMemberRelationEntity::getMemberRelationId).toList());
        }
    }

    @Override
    public void removeByBillIds(List<Long> billIds) {
        if (CollectionUtils.isEmpty(billIds)) return;
        LambdaQueryWrapper<FortuneMemberRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberRelationEntity.class);
        wrapper.in(FortuneMemberRelationEntity::getBillId, billIds);
        List<FortuneMemberRelationEntity> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            fortuneMemberRelationMapper.deleteByIds(list.stream().map(FortuneMemberRelationEntity::getMemberRelationId).toList());
        }
    }

    @Override
    public Boolean existByMemberId(Long memberId) {
        LambdaQueryWrapper<FortuneMemberRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberRelationEntity.class);
        wrapper.eq(FortuneMemberRelationEntity::getMemberId, memberId);
        return this.exists(wrapper);
    }
}
