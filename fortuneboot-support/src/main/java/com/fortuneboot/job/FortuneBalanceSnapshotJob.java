package com.fortuneboot.job;

import com.fortuneboot.repository.fortune.FortuneBalanceSnapshotRepo;
import com.fortuneboot.service.fortune.FortuneBalanceSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 账户余额快照任务
 *
 * @author zhangchi118
 * @date 2026/8/4 20:48
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class FortuneBalanceSnapshotJob {

    private static final String REBUILD_LOCK_NAME = "fortune_balance_snapshot_rebuild";
    private static final int LOCK_TIMEOUT_SECONDS = 1;

    private final FortuneBalanceSnapshotService fortuneBalanceSnapshotService;
    private final FortuneBalanceSnapshotRepo fortuneBalanceSnapshotRepo;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(cron = "${fortune.balance-snapshot.cron:0 10 3 * * ?}")
    public void rebuildDailySnapshots() {
        if (!running.compareAndSet(false, true)) {
            log.warn("账户余额每日快照重算仍在执行，本次任务跳过");
            return;
        }
        if (!fortuneBalanceSnapshotRepo.tryLock(REBUILD_LOCK_NAME, LOCK_TIMEOUT_SECONDS)) {
            running.set(false);
            log.warn("账户余额每日快照重算跳过：未获取到数据库锁");
            return;
        }
        try {
            log.info("开始重算账户余额每日快照");
            fortuneBalanceSnapshotService.rebuildDailySnapshots();
            log.info("账户余额每日快照重算完成");
        } finally {
            fortuneBalanceSnapshotRepo.releaseLock(REBUILD_LOCK_NAME);
            running.set(false);
        }
    }
}
