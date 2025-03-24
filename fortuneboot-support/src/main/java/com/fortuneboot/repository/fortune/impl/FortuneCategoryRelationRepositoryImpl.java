package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.common.DeleteEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneCategoryRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类账单关系
 *
 * @author zhangchi118
 * @date 2025/1/29 23:31
 **/
@Service
@AllArgsConstructor
public class FortuneCategoryRelationRepositoryImpl extends ServiceImpl<FortuneCategoryRelationMapper, FortuneCategoryRelationEntity> implements FortuneCategoryRelationRepository {

    private final FortuneCategoryRelationMapper fortuneCategoryRelationMapper;

    @Override
    public List<FortuneCategoryRelationEntity> getByBillId(Long billId) {
        LambdaQueryWrapper<FortuneCategoryRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryRelationEntity.class);
        wrapper.eq(FortuneCategoryRelationEntity::getBillId, billId);
        return this.list(wrapper);
    }

    @Override
    public Map<Long, List<FortuneCategoryRelationEntity>> getByBillIdList(List<Long> billIdList) {
        LambdaQueryWrapper<FortuneCategoryRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryRelationEntity.class);
        wrapper.in(FortuneCategoryRelationEntity::getBillId, billIdList);
        List<FortuneCategoryRelationEntity> list = this.list(wrapper);
        return list.stream().collect(Collectors.groupingBy(FortuneCategoryRelationEntity::getBillId));
    }

    @Override
    public void removeByBillIds(List<Long> billIds) {
        LambdaQueryWrapper<FortuneCategoryRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryRelationEntity.class);
        wrapper.in(FortuneCategoryRelationEntity::getBillId, billIds);
        List<FortuneCategoryRelationEntity> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)){
            return;
        }
        List<Long> ids = list.stream().map(FortuneCategoryRelationEntity::getCategoryRelationId).toList();
        fortuneCategoryRelationMapper.deleteBatchIds(ids);
    }

    @Override
    public void removeByBillId(Long billId) {
        LambdaQueryWrapper<FortuneCategoryRelationEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryRelationEntity.class);
        wrapper.eq(FortuneCategoryRelationEntity::getBillId, billId);
        List<FortuneCategoryRelationEntity> relationList = this.list(wrapper);
        if (CollectionUtils.isEmpty(relationList)){
            return;
        }
        List<Long> list = relationList.stream().map(FortuneCategoryRelationEntity::getCategoryRelationId).toList();
        fortuneCategoryRelationMapper.deleteBatchIds(list);
    }
    @Override
    public Boolean existByCategoryId(Long categoryId) {
        LambdaQueryWrapper<FortuneCategoryRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneCategoryRelationEntity.class);
        queryWrapper.eq(FortuneCategoryRelationEntity::getCategoryId,categoryId);
        return this.exists(queryWrapper);
    }
}
