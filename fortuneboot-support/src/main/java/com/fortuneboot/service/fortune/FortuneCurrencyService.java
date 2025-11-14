package com.fortuneboot.service.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.entity.fortune.FortuneCurrencyEntity;
import com.fortuneboot.domain.query.fortune.FortuneCurrencyQuery;
import com.fortuneboot.repository.fortune.FortuneCurrencyRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 汇率服务类.
 * @author zhangchi118
 * @date 2025/4/4 20:47
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneCurrencyService {

    private final ApplicationScopeBo applicationScopeBo;

    private final FortuneCurrencyRepo fortuneCurrencyRepo;

    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledFuture;

    /**
     * 初始化货币
     */
    public void loadCurrencyTemplate() {
        List<CurrencyTemplateBo> currencyDetailsList = new ArrayList<>();
        try {
            List<FortuneCurrencyEntity> currencyEntityList = fortuneCurrencyRepo.getAll();
            if (CollectionUtils.isEmpty(currencyEntityList)) {
                Resource resource = new ClassPathResource("currency-template.json");
                ObjectMapper objectMapper = new ObjectMapper();
                currencyDetailsList = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
                });
            }else {
                currencyDetailsList = BeanUtil.copyToList(currencyEntityList, CurrencyTemplateBo.class);
            }
        } catch (Exception e) {
            log.error("初始化货币失败：", e);
            currencyDetailsList.add(new CurrencyTemplateBo(1L, "USD", new BigDecimal("1.0")));
            currencyDetailsList.add(new CurrencyTemplateBo(2L, "CNY", new BigDecimal("7.25")));
        }
        applicationScopeBo.setCurrencyTemplateBoList(currencyDetailsList);
    }


    /**
     * 刷新汇率
     */
    public void refreshCurrency() {
        try {
            // 刷新汇率
            String res = RestClient.builder()
                    .baseUrl("https://api.exchangerate-api.com")
                    .build()
                    .get()
                    .uri("/v4/latest/USD")
                    .retrieve()
                    .body(String.class);
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
            fortuneCurrencyRepo.saveOrUpdateBatch(list);
        } catch (Exception e) {
            log.error("刷新汇率失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化调度
     */
    public void scheduleDailyTask() {
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

    public List<CurrencyTemplateBo> getCurrencyTemplateBoList(FortuneCurrencyQuery query) {
        List<CurrencyTemplateBo> allCurrencies = applicationScopeBo.getCurrencyTemplateBoList();
        // 找到基准汇率
        Optional<CurrencyTemplateBo> baseCurrencyOpt = findBaseCurrency(allCurrencies,query.getBase());
        // 过滤汇率
        List<CurrencyTemplateBo> filteredCurrencies = filterCurrenciesByTarget(allCurrencies, query.getTarget());
        return baseCurrencyOpt.map(baseCurrency -> adjustCurrencyRates(filteredCurrencies, baseCurrency))
                .orElse(filteredCurrencies);
    }

    private Optional<CurrencyTemplateBo> findBaseCurrency(List<CurrencyTemplateBo> currencies, String baseCurrencyName) {
        String finalBaseCurrencyName = StringUtils.isEmpty(baseCurrencyName) ? "USD" : baseCurrencyName;
        return currencies.stream()
                .filter(currency -> currency.getCurrencyName().equals(finalBaseCurrencyName))
                .findFirst();
    }

    private List<CurrencyTemplateBo> filterCurrenciesByTarget(List<CurrencyTemplateBo> currencies, String target) {
        if (ObjectUtil.isEmpty(target)) {
            // 返回新列表避免修改原始集合
            return new ArrayList<>(currencies);
        }
        return currencies.stream()
                .filter(currency -> currency.getCurrencyName().contains(target))
                .toList();
    }

    private List<CurrencyTemplateBo> adjustCurrencyRates(List<CurrencyTemplateBo> currencies, CurrencyTemplateBo baseCurrency) {
        return currencies.stream()
                .map(currency -> createAdjustedCurrency(currency, baseCurrency.getRate()))
                .toList();
    }

    private CurrencyTemplateBo createAdjustedCurrency(CurrencyTemplateBo original, BigDecimal baseRate) {
        CurrencyTemplateBo adjustedCurrency = new CurrencyTemplateBo();
        adjustedCurrency.setCurrencyName(original.getCurrencyName());
        // 复制其他必要属性（根据实际类结构补充）
        BigDecimal newRate = original.getRate().divide(baseRate, 2, RoundingMode.HALF_UP);
        adjustedCurrency.setRate(newRate);
        return adjustedCurrency;
    }
}
