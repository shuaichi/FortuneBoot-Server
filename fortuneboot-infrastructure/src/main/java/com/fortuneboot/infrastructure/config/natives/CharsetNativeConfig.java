package com.fortuneboot.infrastructure.config.natives;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * GraalVM native image 字符集可达性配置（反射兜底）。
 *
 * <p>Apache POI 的 {@code org.apache.poi.util.LocaleUtil} 静态初始化器会调用
 * {@code Charset.forName("CP1252")}，实现类为 {@code sun.nio.cs.MS1252}。GraalVM native image
 * 默认只把 UTF-8/ISO-8859-1/US-ASCII/UTF-16 等标准字符集纳入运行时注册表，CP1252 不在其中，
 * 运行时抛 {@code UnsupportedCharsetException}，连锁引发 {@code ExceptionInInitializerError}，
 * {@code XSSFWorkbook} 无法构造，Excel 模板下载/上传全部失败。
 *
 * <p>主要修复在 native-image 构建参数 {@code -H:+AddAllCharsets}（把全部字符集纳入运行时注册表）。
 * 本类作为反射可达性兜底：通过 Spring AOT {@link RuntimeHints} 注册字符集实现类的反射可达性，
 * 避免极端裁剪场景下实现类被移除。
 *
 * @author zhangchi118
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(CharsetNativeConfig.CharsetRegistrar.class)
public class CharsetNativeConfig {

    /** CP1252 等扩展字符集实现类全限定名，用于反射可达性兜底注册。 */
    private static final String[] REQUIRED_CHARSET_CLASSES = {
            "sun.nio.cs.MS1252"
    };

    public static class CharsetRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            for (String className : REQUIRED_CHARSET_CLASSES) {
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    hints.reflection().registerType(clazz,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.DECLARED_FIELDS,
                            MemberCategory.PUBLIC_FIELDS);
                } catch (ClassNotFoundException e) {
                    System.err.println("[CharsetNativeConfig] charset class not found on classpath: " + className);
                }
            }
        }
    }
}
