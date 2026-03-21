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
            // 1. 注册核心静态资源和 MyBatis 的 XML 文件
            hints.resources().registerPattern("book-template.json");
            hints.resources().registerPattern("currency-template.json");
            hints.resources().registerPattern("i18n/messages*.properties");
            hints.resources().registerPattern("ip2region.xdb");
            hints.resources().registerPattern("banner.txt");
            // 防止手写 SQL 报 Invalid bound statement
            hints.resources().registerPattern("mapper/**/*.xml");

            // 2. 注册 JJWT 强依赖的反射加载类
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.KeysBridge"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonSerializer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonDeserializer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.DeflateCompressionCodec"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.GzipCompressionCodec"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // ================= 补充 Jwts$SIG 等底层类的反射装配 =================
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardKeyOperations"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardKeyAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.io.StandardCompressionAlgorithms"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // 追加缺失的 DefaultClaimsBuilder 和 DefaultHeaderBuilder
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaimsBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultHeaderBuilder"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaimsBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultHeaderBuilder$Supplier"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // 3. 注册 Kaptcha (验证码) 反射类
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.ShadowGimpy"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.NoNoise"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.WaterRipple"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.text.impl.DefaultWordRenderer"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.impl.DefaultBackground"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.google.code.kaptcha.util.Config"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.infrastructure.config.captcha.CaptchaMathTextCreator"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

            // 4. 注册 Quartz 任务反射类
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.job.FortuneRecurringBillJob"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);

            // 5. 注册 Druid 连接池拦截器反射类（防止生产环境连不上数据库）
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.filter.stat.StatFilter"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.wall.WallFilter"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.filter.logging.Slf4jLogFilter"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.resources().registerPattern("support/http/resources/**");

            // 6. 补充 Hutool 常用反射
            hints.reflection().registerType(TypeReference.of("cn.hutool.core.map.MapUtil"), MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("cn.hutool.core.util.StrUtil"), MemberCategory.INVOKE_PUBLIC_METHODS);

            // 7. 补充 Spring Security 权限类反射（防止 Jackson 反序列化由于缺少反射配置报错）
            hints.reflection().registerType(TypeReference.of("org.springframework.security.core.authority.SimpleGrantedAuthority"), MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS);

            // 8. 将 JNA 所需的各个操作系统的底层动态链接库资源打包进镜像
            hints.resources().registerPattern("com/sun/jna/**");

            // 9. 注册 OSHI 硬件监控所需的核心平台反射类（OSHI会根据系统自动反射实例化这些类）
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