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
}
