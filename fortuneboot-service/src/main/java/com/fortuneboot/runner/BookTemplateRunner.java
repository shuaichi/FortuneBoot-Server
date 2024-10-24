package com.fortuneboot.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.BookTemplateBo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 账本模板Runner
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 00:15
 **/
@Component
@RequiredArgsConstructor
public class BookTemplateRunner implements ApplicationRunner {

    private final ApplicationScopeBo applicationScopeBo;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Resource resource = new ClassPathResource("book-template.json");
            ObjectMapper objectMapper = new ObjectMapper();
            BookTemplateBo[] bookTemplateList = objectMapper.readValue(resource.getInputStream(), BookTemplateBo[].class);
            applicationScopeBo.setBookTemplateBoList(Arrays.asList(bookTemplateList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
