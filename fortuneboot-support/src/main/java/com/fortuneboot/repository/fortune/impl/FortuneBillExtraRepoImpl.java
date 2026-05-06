package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneBillExtraMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 账单附加费用
 *
 * @author zhangchi118
 **/
@Service
@AllArgsConstructor
public class FortuneBillExtraRepoImpl extends ServiceImpl<FortuneBillExtraMapper, FortuneBillExtraEntity> implements FortuneBillExtraRepo {

    private final FortuneBillExtraMapper fortuneBillExtraMapper;

    @Override
    public List<FortuneBillExtraEntity> getByBillId(Long billId) {
        if (Objects.isNull(billId)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<FortuneBillExtraEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillExtraEntity.class);
        wrapper.eq(FortuneBillExtraEntity::getBillId, billId);
        return this.list(wrapper);
    }

    @Override
    public Map<Long, List<FortuneBillExtraEntity>> getByBillIdList(List<Long> billIdList) {
        if (CollectionUtils.isEmpty(billIdList)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<FortuneBillExtraEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillExtraEntity.class);
        wrapper.in(FortuneBillExtraEntity::getBillId, billIdList);
        List<FortuneBillExtraEntity> list = this.list(wrapper);
        return list.stream().collect(Collectors.groupingBy(FortuneBillExtraEntity::getBillId));
    }

    @Override
    public void phyRemoveByBillId(Long billId) {
        if (Objects.isNull(billId)) {
            return;
        }
        List<FortuneBillExtraEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneBillExtraEntity::getExtraId).toList();
        fortuneBillExtraMapper.phyDeleteByIds(ids);
    }

    @Override
    public void removeByBillIds(List<Long> billIds) {
        if (CollectionUtils.isEmpty(billIds)) {
            return;
        }
        LambdaQueryWrapper<FortuneBillExtraEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillExtraEntity.class);
        wrapper.in(FortuneBillExtraEntity::getBillId, billIds);
        List<FortuneBillExtraEntity> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneBillExtraEntity::getExtraId).toList();
        fortuneBillExtraMapper.deleteByIds(ids);
    }
}