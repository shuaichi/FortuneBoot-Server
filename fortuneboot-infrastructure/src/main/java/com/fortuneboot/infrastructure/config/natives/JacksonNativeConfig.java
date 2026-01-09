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
@ImportRuntimeHints(JacksonNativeConfig.JacksonReflectionRegistrar.class) // 引入刚才写的注册器
public class JacksonNativeConfig {

    public static class JacksonReflectionRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. 指定你要扫描的包路径
            String basePackage = "com.fortuneboot.domain";

            // 2. 使用 Spring 的扫描工具
            ClassPathScanningCandidateComponentProvider scanner =
                    new ClassPathScanningCandidateComponentProvider(false);

            // 这里的 Filter 可以根据需要修改，比如扫描所有 Object (即所有类)
            scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

            Set<BeanDefinition> definitions = scanner.findCandidateComponents(basePackage);

            for (BeanDefinition bd : definitions) {
                try {
                    Class<?> clazz = ClassUtils.forName(bd.getBeanClassName(), classLoader);
                    // 3. 注册反射能力：保留构造函数、字段、方法等（对应 Jackson 需要的能力）
                    hints.reflection().registerType(clazz,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.ACCESS_DECLARED_FIELDS);

                    System.out.println("Native Image 自动注册反射: " + clazz.getName());
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
    }
}
