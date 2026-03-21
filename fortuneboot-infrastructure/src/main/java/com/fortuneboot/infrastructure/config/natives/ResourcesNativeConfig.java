package com.fortuneboot.infrastructure.config.natives;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * 集中注册资源文件及第三方强依赖反射的类
 *
 * @author zhangchi118
 * @date 2026/1/9 10:39
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(ResourcesNativeConfig.FortuneBootResourceHints.class)
public class ResourcesNativeConfig {

    public static class FortuneBootResourceHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // 1. 注册静态资源文件
            hints.resources().registerPattern("book-template.json");
            hints.resources().registerPattern("currency-template.json");
            // 修复 i18n 资源找不到的问题
            hints.resources().registerPattern("i18n/messages*.properties");
            // 修复离线 IP 属地解析库找不到的问题
            hints.resources().registerPattern("ip2region.xdb");
            hints.resources().registerPattern("banner.txt");

            // 2. 注册 JJWT 强依赖的反射加载类（解决 KeysBridge 导致直接崩溃的问题）
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.KeysBridge"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonSerializer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonDeserializer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.DeflateCompressionCodec"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.GzipCompressionCodec"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // 3. 注册 Kaptcha (验证码) 反射类，防止获取验证码时抛出反射异常
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.ShadowGimpy"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.NoNoise"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.WaterRipple"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.text.impl.DefaultWordRenderer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.DefaultBackground"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.util.Config"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            // 注册你自定义的验证码生成类
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.infrastructure.config.captcha.CaptchaMathTextCreator"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

            // 4. 注册 Quartz 任务反射类
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.job.FortuneRecurringBillJob"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

            // 5. 补充 Hutool 常用反射
            hints.reflection().registerType(TypeReference.of("cn.hutool.core.map.MapUtil"), MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("cn.hutool.core.util.StrUtil"), MemberCategory.INVOKE_PUBLIC_METHODS);
        }
    }
}