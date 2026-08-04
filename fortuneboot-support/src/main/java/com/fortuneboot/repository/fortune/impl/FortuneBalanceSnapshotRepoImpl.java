package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneBalanceSnapshotMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBalanceSnapshotEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneLineVo;
import com.fortuneboot.repository.fortune.FortuneBalanceSnapshotRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 账户余额快照Repository
 *
 * @author zhangchi118
 * @date 2026/8/4 20:10
 **/
@Service
@AllArgsConstructor
public class FortuneBalanceSnapshotRepoImpl
        extends ServiceImpl<FortuneBalanceSnapshotMapper, FortuneBalanceSnapshotEntity>
        implements FortuneBalanceSnapshotRepo {

    private final FortuneBalanceSnapshotMapper fortuneBalanceSnapshotMapper;

    @Override
    public void removeBySnapshotDate(LocalDate snapshotDate) {
        fortuneBalanceSnapshotMapper.deleteBySnapshotDate(snapshotDate);
    }

    @Override
    public List<FortuneLineVo> getNetAssetsTrend(Long groupId, Integer periodType) {
        return fortuneBalanceSnapshotMapper.getNetAssetsTrend(groupId, periodType);
    }

    @Override
    public List<FortuneLineVo> getAccountBalanceTrend(Long accountId, Integer periodType) {
        return fortuneBalanceSnapshotMapper.getAccountBalanceTrend(accountId, periodType);
    }
}
