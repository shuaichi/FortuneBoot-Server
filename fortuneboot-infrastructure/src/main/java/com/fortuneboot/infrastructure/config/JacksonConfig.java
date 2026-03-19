package com.fortuneboot.infrastructure.config;

import com.fortuneboot.infrastructure.security.xss.JsonHtmlXssTrimSerializer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.TimeZone;

/**
 * @author valarchie
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig implements JsonMapperBuilderCustomizer {

    @Override
    public void customize(JsonMapper.Builder jsonMapperBuilder) {
        // 【修复：XSS过滤器破坏业务】注销掉全局 String HtmlUtil.cleanHtmlTag，前端输出时做转义即可
        /*
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(String.class, new JsonHtmlXssTrimSerializer());
        jsonMapperBuilder.addModules(simpleModule);
        */
        jsonMapperBuilder.defaultTimeZone(TimeZone.getDefault());
    }
}