package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneAccountMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneAssetsLiabilitiesVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账户
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:04
 **/
@Service
@AllArgsConstructor
public class FortuneAccountRepoImpl extends ServiceImpl<FortuneAccountMapper, FortuneAccountEntity> implements FortuneAccountRepo {

    private final FortuneAccountMapper fortuneAccountMapper;

    @Override
    public List<FortuneAccountEntity> getEnableAccountList(Long groupId) {
        LambdaQueryWrapper<FortuneAccountEntity> lambdaQueryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneAccountEntity.class);
        lambdaQueryWrapper.eq(FortuneAccountEntity::getGroupId,groupId)
                .eq(FortuneAccountEntity::getEnable,Boolean.TRUE)
                .eq(FortuneAccountEntity::getRecycleBin,Boolean.FALSE);
        return this.list(lambdaQueryWrapper);
    }

    @Override
    public List<FortuneAccountEntity> getByIds(List<Long> accountIdList) {
        LambdaQueryWrapper<FortuneAccountEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneAccountEntity.class);
        queryWrapper.in(FortuneAccountEntity::getAccountId,accountIdList);
        return this.list(queryWrapper);
    }

    @Override
    public List<FortunePieVo> getTotalAssets(Long groupId) {
        return fortuneAccountMapper.getTotalAssets(groupId);
    }

    @Override
    public List<FortunePieVo> getTotalLiabilities(Long groupId) {
        return fortuneAccountMapper.getTotalLiabilities(groupId);
    }

    @Override
    public FortuneAssetsLiabilitiesVo getFortuneAssetsLiabilities(Long groupId) {
        return fortuneAccountMapper.getFortuneAssetsLiabilities(groupId);
    }
}
