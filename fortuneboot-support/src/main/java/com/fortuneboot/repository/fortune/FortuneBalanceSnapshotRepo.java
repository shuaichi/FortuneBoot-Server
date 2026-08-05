package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneBalanceSnapshotEntity;
import com.fortuneboot.domain.vo.fortune.include.FortuneLineVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 账户余额快照Repository
 *
 * @author zhangchi118
 * @date 2026/8/4 20:09
 **/
public interface FortuneBalanceSnapshotRepo extends IService<FortuneBalanceSnapshotEntity> {

    void removeBySnapshotDate(LocalDate snapshotDate);

    void removeBySnapshotDateBetween(LocalDate startDate, LocalDate endDate);

    void removeAllSnapshots();

    boolean tryLock(String lockName, int timeoutSeconds);

    void releaseLock(String lockName);

    List<FortuneLineVo> getNetAssetsTrend(Long groupId, Integer periodType);

    List<FortuneLineVo> getAccountBalanceTrend(Long accountId, Integer periodType);
}
