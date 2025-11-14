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
        SimpleModule simpleModule = new SimpleModule();
        // 如果你的 JsonHtmlXssTrimSerializer 是 JsonDeserializer<String>（针对 String），就这样注册：
        simpleModule.addDeserializer(String.class, new JsonHtmlXssTrimSerializer());
        // 如果它针对的是其他类型（比如某个 DTO），把 String.class 换成对应的 Class
        jsonMapperBuilder.addModules(simpleModule);

        jsonMapperBuilder.defaultTimeZone(TimeZone.getDefault());
    }
}