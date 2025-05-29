package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneTagRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交易标签和账单的关系表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:37
 **/
@Service
@AllArgsConstructor
public class FortuneTagRelationRepositionImpl extends ServiceImpl<FortuneTagRelationMapper, FortuneTagRelationEntity> implements FortuneTagRelationRepository {

    private final FortuneTagRelationMapper fortuneTagRelationMapper;

    @Override
    public List<FortuneTagRelationEntity> getByBillId(Long billId) {
        LambdaQueryWrapper<FortuneTagRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagRelationEntity.class);
        wrapper.eq(FortuneTagRelationEntity::getBillId, billId);
        return this.list(wrapper);
    }

    @Override
    public Map<Long, List<FortuneTagRelationEntity>> getByBillIdList(List<Long> billIdList) {
        LambdaQueryWrapper<FortuneTagRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagRelationEntity.class);
        wrapper.in(FortuneTagRelationEntity::getBillId, billIdList);
        List<FortuneTagRelationEntity> list = this.list(wrapper);
        return list.stream().collect(Collectors.groupingBy(FortuneTagRelationEntity::getBillId));
    }

    @Override
    public void removeByBillId(Long billId) {
        List<FortuneTagRelationEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneTagRelationEntity::getTagRelationId).toList();
        fortuneTagRelationMapper.deleteByIds(ids);
    }

    @Override
    public void phyRemoveByBillId(Long billId) {
        List<FortuneTagRelationEntity> list = this.getByBillId(billId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneTagRelationEntity::getTagRelationId).toList();
        fortuneTagRelationMapper.phyDeleteByBillId(ids);
    }

    @Override
    public void removeByBillIds(List<Long> billIds) {
        LambdaQueryWrapper<FortuneTagRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagRelationEntity.class);
        queryWrapper.in(FortuneTagRelationEntity::getBillId, billIds);
        List<FortuneTagRelationEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> ids = list.stream().map(FortuneTagRelationEntity::getTagRelationId).toList();
        fortuneTagRelationMapper.deleteByIds(ids);
    }

    @Override
    public Boolean existByTagId(Long tagId) {
        LambdaQueryWrapper<FortuneTagRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagRelationEntity.class);
        queryWrapper.eq(FortuneTagRelationEntity::getTagId, tagId);
        return this.exists(queryWrapper);
    }
}
