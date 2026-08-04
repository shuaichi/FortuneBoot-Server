package com.fortuneboot.job;

import com.fortuneboot.service.fortune.FortuneBalanceSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    private final FortuneBalanceSnapshotService fortuneBalanceSnapshotService;

    @Scheduled(cron = "0 10 0 1 * ?")
    public void generateMonthEndSnapshot() {
        log.info("开始生成账户余额月末快照");
        fortuneBalanceSnapshotService.generateMonthEndSnapshot();
        log.info("账户余额月末快照生成完成");
    }
}
