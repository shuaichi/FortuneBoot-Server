package com.fortuneboot.runner;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.entity.fortune.FortuneCurrencyEntity;
import com.fortuneboot.repository.fortune.FortuneCurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyTemplateRunner implements ApplicationRunner {

    private final ApplicationScopeBo applicationScopeBo;

    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledFuture;

    private final RestTemplate restTemplate;

    private final FortuneCurrencyRepository fortuneCurrencyRepository;

    @Override
    public void run(ApplicationArguments args) {
        // 初始化货币
        this.loadCurrencyTemplate();
        // 初始化调度
        this.scheduleDailyTask();
        // 启动后立刻刷新一次汇率
        this.refreshCurrency();
    }

    private void loadCurrencyTemplate() {
        List<CurrencyTemplateBo> currencyDetailsList = new ArrayList<>();
        try {
            List<FortuneCurrencyEntity> currencyEntityList = fortuneCurrencyRepository.getAll();
            currencyDetailsList = BeanUtil.copyToList(currencyEntityList, CurrencyTemplateBo.class);
        } catch (Exception e) {
            log.error("初始化货币失败：", e);
            currencyDetailsList.add(new CurrencyTemplateBo(1L, "USD", new BigDecimal("1.0")));
            currencyDetailsList.add(new CurrencyTemplateBo(2L, "CNY", new BigDecimal("7.25")));
        }
        applicationScopeBo.setCurrencyTemplateBoList(currencyDetailsList);
    }

    public void refreshCurrency() {
        try {
            // 刷新汇率
            String res = restTemplate.getForObject("https://api.exchangerate-api.com/v4/latest/USD", String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(res);
            JsonNode rates = root.get("rates");
            Map<String, BigDecimal> rateMap = new HashMap<>();
            // 转成map
            rates.fields().forEachRemaining(entry -> {
                BigDecimal value = entry.getValue().decimalValue();
                rateMap.put(entry.getKey(), value);
            });
            List<CurrencyTemplateBo> currencyTemplateBoList = applicationScopeBo.getCurrencyTemplateBoList();
            // 刷新内存中数据
            rateMap.forEach((key, value) -> {
                Optional<CurrencyTemplateBo> currencyTemplateOptional = currencyTemplateBoList.stream()
                        .filter(user -> user.getCurrencyName().equals(key))
                        .findFirst();
                if (currencyTemplateOptional.isPresent()) {
                    CurrencyTemplateBo currencyTemplateBo = currencyTemplateOptional.get();
                    currencyTemplateBo.setRate(value);
                }
            });
            // 持久化到数据库
            List<FortuneCurrencyEntity> list = BeanUtil.copyToList(currencyTemplateBoList, FortuneCurrencyEntity.class);
            fortuneCurrencyRepository.saveOrUpdateBatch(list);
        } catch (Exception e) {
            log.error("刷新汇率失败: {}", e.getMessage(), e);
        }
    }


    private void scheduleDailyTask() {
        // 取消已有任务
        if (Objects.nonNull(scheduledFuture)) {
            scheduledFuture.cancel(Boolean.FALSE);
        }
        // 计算下次执行时间
        Instant nextExecution = this.calculateNextExecutionTime();
        scheduledFuture = taskScheduler.schedule(this::executeTask, nextExecution);
    }

    private void executeTask() {
        try {
            this.refreshCurrency();
            this.loadCurrencyTemplate();
        } finally {
            // 每次执行后重新调度
            this.scheduleDailyTask();
        }
    }

    /**
     * 每天随机时间刷新一次汇率
     *
     * @return
     */

    private Instant calculateNextExecutionTime() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        // 次日零点基准
        ZonedDateTime baseTime = now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(now.getZone());
        // 生成随机分钟偏移（0-1439）
        int randomMinutes = ThreadLocalRandom.current().nextInt(1440);
        return baseTime.plusMinutes(randomMinutes).toInstant();
    }
}
