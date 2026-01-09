package com.fortuneboot.infrastructure.config.natives;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 *
 * @author zhangchi118
 * @date 2026/1/9 10:39
 **/
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(ResourcesNativeConfig.BookTemplateResourceHints.class)
public class ResourcesNativeConfig {

    public static class BookTemplateResourceHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.resources().registerPattern("book-template.json");
            hints.resources().registerPattern("currency-template.json");
        }
    }
}
