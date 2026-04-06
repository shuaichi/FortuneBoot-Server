package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneMemberMapper;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:30
 **/
@Service
@AllArgsConstructor
public class FortuneMemberRepoImpl extends ServiceImpl<FortuneMemberMapper, FortuneMemberEntity> implements FortuneMemberRepo {

    private final FortuneMemberMapper fortuneMemberMapper;

    @Override
    public FortuneMemberEntity getByBookIdAndName(Long bookId, String memberName) {
        LambdaQueryWrapper<FortuneMemberEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberEntity.class);
        wrapper.eq(FortuneMemberEntity::getBookId, bookId)
                .eq(FortuneMemberEntity::getMemberName, memberName);
        return this.getOne(wrapper);
    }

    @Override
    public List<FortuneMemberEntity> getEnableMemberList(Long bookId) {
        LambdaQueryWrapper<FortuneMemberEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberEntity.class);
        wrapper.eq(FortuneMemberEntity::getBookId, bookId)
                .eq(FortuneMemberEntity::getEnable, Boolean.TRUE)
                .eq(FortuneMemberEntity::getRecycleBin, Boolean.FALSE)
                .orderByAsc(FortuneMemberEntity::getSort);
        return this.list(wrapper);
    }

    @Override
    public List<FortuneMemberEntity> getByIds(List<Long> memberIds) {
        LambdaQueryWrapper<FortuneMemberEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberEntity.class);
        wrapper.in(FortuneMemberEntity::getMemberId, memberIds);
        return this.list(wrapper);
    }

    @Override
    public void removeByBookId(Long bookId) {
        LambdaQueryWrapper<FortuneMemberEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberEntity.class);
        wrapper.eq(FortuneMemberEntity::getBookId, bookId);
        List<FortuneMemberEntity> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            fortuneMemberMapper.deleteByIds(list.stream().map(FortuneMemberEntity::getMemberId).toList());
        }
    }

    @Override
    public void removeByBookIds(List<Long> bookIds) {
        LambdaQueryWrapper<FortuneMemberEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneMemberEntity.class);
        wrapper.in(FortuneMemberEntity::getBookId, bookIds);
        List<FortuneMemberEntity> list = this.list(wrapper);
        if (CollectionUtils.isNotEmpty(list)) {
            fortuneMemberMapper.deleteByIds(list.stream().map(FortuneMemberEntity::getMemberId).toList());
        }
    }
}