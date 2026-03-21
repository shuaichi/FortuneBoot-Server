package com.fortuneboot.infrastructure.config.natives;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.Set;

/**
 *
 * @author zhangchi118
 * @date 2026/1/9 13:15
 **/
@Slf4j
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(JacksonNativeConfig.JacksonReflectionRegistrar.class)
public class JacksonNativeConfig {

    public static class JacksonReflectionRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 【核心修复】将作用域扩大到系统根目录，这样即使 DTO 写在了 service 包下也不会报空
            var scanList = Collections.singletonList("com.fortuneboot");

            for (Object basePkg : scanList) {
                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false);

                scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

                Set<BeanDefinition> definitions = scanner.findCandidateComponents(basePkg.toString());

                for (BeanDefinition bd : definitions) {
                    try {
                        Class<?> clazz = ClassUtils.forName(bd.getBeanClassName(), classLoader);
                        // 注册 Jackson 和 Hutool 序列化需要的反射能力
                        hints.reflection().registerType(clazz,
                                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                                MemberCategory.INVOKE_PUBLIC_METHODS,
                                MemberCategory.ACCESS_DECLARED_FIELDS);
                    } catch (ClassNotFoundException e) {
                        // ignore
                        log.error("JacksonNativeConfig.registerHints exception = ",e);
                    }
                }
            }
        }
    }
}