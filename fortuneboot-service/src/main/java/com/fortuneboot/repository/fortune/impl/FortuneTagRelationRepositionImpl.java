package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneTagRelationMapper;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
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
public class FortuneTagRelationRepositionImpl extends ServiceImpl<FortuneTagRelationMapper, FortuneTagRelationEntity> implements FortuneTagRelationRepository {

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
    public void removeByBillIds(List<Long> billIds) {
        LambdaQueryWrapper<FortuneTagRelationEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneTagRelationEntity.class);
        queryWrapper.in(FortuneTagRelationEntity::getBillId,billIds);
        this.remove(queryWrapper);
    }
}
