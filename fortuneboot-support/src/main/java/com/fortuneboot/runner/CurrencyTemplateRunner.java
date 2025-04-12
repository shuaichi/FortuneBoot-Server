package com.fortuneboot.runner;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.entity.fortune.FortuneCurrencyEntity;
import com.fortuneboot.repository.fortune.FortuneCurrencyRepository;
import com.fortuneboot.service.fortune.FortuneCurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

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
