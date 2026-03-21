package com.fortuneboot.infrastructure.config.natives;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 *
 * @author zhangchi118
 * @date 2026/1/9 13:15
 **/
@Configuration(proxyBeanMethods = false)
// 引入刚才写的注册器
@ImportRuntimeHints(JacksonNativeConfig.JacksonReflectionRegistrar.class)
public class JacksonNativeConfig {

    public static class JacksonReflectionRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            // 使用 var 避开前端渲染 BUG，同时将需要序列化的基础包全部囊括
            var packagesToScan = java.util.Arrays.asList(
                    "com.fortuneboot.domain",
                    "com.fortuneboot.common",
                    "com.fortuneboot.infrastructure"
            );

            for (Object basePkg : packagesToScan) {
                ClassPathScanningCandidateComponentProvider scanner =
                        new ClassPathScanningCandidateComponentProvider(false);

                scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

                Set<BeanDefinition> definitions = scanner.findCandidateComponents(basePkg.toString());

                for (BeanDefinition bd : definitions) {
                    try {
                        Class<?> clazz = ClassUtils.forName(bd.getBeanClassName(), classLoader);
                        // 注册 Jackson 序列化需要的反射能力
                        hints.reflection().registerType(clazz,
                                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                                MemberCategory.INVOKE_PUBLIC_METHODS,
                                MemberCategory.ACCESS_DECLARED_FIELDS);
                    } catch (ClassNotFoundException e) {
                        // ignore
                    }
                }
            }
        }
    }
}
