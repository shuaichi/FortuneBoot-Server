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

            // ================= 1. 静态资源与模板文件 =================
            hints.resources().registerPattern("book-template.json");
            hints.resources().registerPattern("currency-template.json");
            hints.resources().registerPattern("i18n/messages*.properties");
            hints.resources().registerPattern("ip2region.xdb");
            hints.resources().registerPattern("banner.txt");
            hints.resources().registerPattern("mapper/**/*.xml"); // 防止手写 SQL 报 Invalid bound statement
            hints.resources().registerPattern("support/http/resources/**"); // Druid 监控台静态资源
            hints.resources().registerPattern("com/sun/jna/**"); // 【关键】JNA 底层 C 动态链接库 (.so / .dll)

            // ================= 2. JWT (JSON Web Token) 相关反射 =================
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.KeysBridge"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonSerializer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonDeserializer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.DeflateCompressionCodec"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.GzipCompressionCodec"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardKeyOperations"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardKeyAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.io.StandardCompressionAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaimsBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultHeaderBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaimsBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultHeaderBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // ================= 3. Kaptcha (验证码) 相关反射 =================
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.ShadowGimpy"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.NoNoise"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.WaterRipple"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.text.impl.DefaultWordRenderer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.DefaultBackground"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.util.Config"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.infrastructure.config.captcha.CaptchaMathTextCreator"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

            // ================= 4. 定时任务、第三方基础工具、Druid 过滤器 =================
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.job.FortuneRecurringBillJob"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.filter.stat.StatFilter"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.wall.WallFilter"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.filter.logging.Slf4jLogFilter"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("cn.hutool.core.map.MapUtil"), MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("cn.hutool.core.util.StrUtil"), MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("org.springframework.security.core.authority.SimpleGrantedAuthority"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS);

            // ================= 5. OSHI 与 JNA (服务器监控核心) =================

            // 5.1 注册 JNA JDK 动态代理 (C语言接口代理)
            hints.proxies().registerJdkProxy(TypeReference.of("oshi.jna.platform.linux.LinuxLibc"));
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.platform.linux.LibC"));
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.platform.linux.LibRT"));
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.platform.linux.Udev"));
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.Library"));

            // Windows/Mac 的核心代理也加上，方便以后跨平台部署
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.platform.win32.Kernel32"));
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.platform.win32.Advapi32"));
            hints.proxies().registerJdkProxy(TypeReference.of("com.sun.jna.platform.mac.SystemB"));
            // 5.2 注册 JNA 结构体 (Structure) 反射（解决磁盘读取时的 getFieldOrder 报错）
            String[] jnaStructures = new String[]{
                    "oshi.jna.platform.linux.LinuxLibc$Sysinfo",
                    "oshi.jna.platform.linux.LinuxLibc$Statvfs",
                    "com.sun.jna.platform.linux.LibC$Sysinfo",
                    "com.sun.jna.platform.linux.LibC$Statvfs",
                    "com.sun.jna.platform.linux.Udev$UdevDevice",
                    "com.sun.jna.platform.linux.Udev$UdevListEntry",
                    "com.sun.jna.platform.linux.Udev$UdevContext",
                    "com.sun.jna.platform.linux.Udev$UdevEnumerate"
            };

            for (String struct : jnaStructures) {
                hints.reflection().registerType(TypeReference.of(struct),
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        // JNA 必须通过 public 字段的顺序来对齐 C 语言结构体的内存！
                        MemberCategory.ACCESS_PUBLIC_FIELDS,
                        MemberCategory.ACCESS_DECLARED_FIELDS);
            }

            // 5.3 注册 JNA 核心底层类的反射调用
            hints.reflection().registerType(TypeReference.of("com.sun.jna.Native"), MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS);
            hints.reflection().registerType(TypeReference.of("com.sun.jna.Structure"), MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS);
            hints.reflection().registerType(TypeReference.of("com.sun.jna.Pointer"), MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS);

            // 5.4 注册 OSHI 硬件监控所需的核心平台反射类（OSHI会根据系统自动反射实例化这些类）
            hints.reflection().registerType(TypeReference.of("oshi.SystemInfo"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("oshi.hardware.platform.linux.LinuxHardwareAbstractionLayer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("oshi.software.os.linux.LinuxOperatingSystem"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("oshi.hardware.platform.windows.WindowsHardwareAbstractionLayer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("oshi.software.os.windows.WindowsOperatingSystem"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("oshi.hardware.platform.mac.MacHardwareAbstractionLayer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("oshi.software.os.mac.MacOperatingSystem"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

        }
    }
}