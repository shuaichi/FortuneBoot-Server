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
    public void removeBySnapshotDateBetween(LocalDate startDate, LocalDate endDate) {
        fortuneBalanceSnapshotMapper.deleteBySnapshotDateBetween(startDate, endDate);
    }

    @Override
    public void removeAllSnapshots() {
        fortuneBalanceSnapshotMapper.deleteAllSnapshots();
    }

    @Override
    public boolean tryLock(String lockName, int timeoutSeconds) {
        Integer result = fortuneBalanceSnapshotMapper.tryLock(lockName, timeoutSeconds);
        return Integer.valueOf(1).equals(result);
    }

    @Override
    public void releaseLock(String lockName) {
        fortuneBalanceSnapshotMapper.releaseLock(lockName);
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
