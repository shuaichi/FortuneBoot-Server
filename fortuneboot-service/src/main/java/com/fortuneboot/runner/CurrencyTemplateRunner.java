package com.fortuneboot.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.CurrencyTemplateBo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void run(ApplicationArguments args) {
        List<CurrencyTemplateBo> currencyDetailsList = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource("currency-template.json");
            ObjectMapper objectMapper = new ObjectMapper();
            currencyDetailsList = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("初始化货币失败：", e);
            currencyDetailsList.add(new CurrencyTemplateBo(1L, "USD", 1.0));
            currencyDetailsList.add(new CurrencyTemplateBo(2L, "CNY", 7.25));
        }
        applicationScopeBo.setCurrencyTemplateBoList(currencyDetailsList);
    }
}
