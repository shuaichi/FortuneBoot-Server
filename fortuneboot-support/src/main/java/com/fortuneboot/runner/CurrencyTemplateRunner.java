package com.fortuneboot.runner;

import com.fortuneboot.service.fortune.FortuneCurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 货币模板Runner
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 00:35
 **/
@Component
@RequiredArgsConstructor
public class CurrencyTemplateRunner implements ApplicationRunner {

    private final FortuneCurrencyService fortuneCurrencyService;
    @Override
    public void run(ApplicationArguments args) {
        // 初始化货币
        fortuneCurrencyService.loadCurrencyTemplate();
        // 初始化调度
        fortuneCurrencyService.scheduleDailyTask();
        // 启动后立刻刷新一次汇率
        fortuneCurrencyService.refreshCurrency();
    }
}
