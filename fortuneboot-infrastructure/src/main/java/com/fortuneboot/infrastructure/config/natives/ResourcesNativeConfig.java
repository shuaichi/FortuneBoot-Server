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

            // ================= 1.0.1 前端静态资源（Vue3 dist 产物） =================
            hints.resources().registerPattern("static/**");

            // ================= 1.1 Flyway 迁移脚本与内部资源 =================
            hints.resources().registerPattern("db/migration/**/*.sql"); // SQL 迁移脚本（MySQL + SQLite）
            hints.resources().registerPattern("org/flywaydb/**"); // Flyway 内部资源文件（配置、版本信息等）
            hints.resources().registerPattern("META-INF/services/org.flywaydb.core.extensibility.Plugin"); // Flyway SPI 插件注册

            // ================= 2. Flyway 数据库迁移 (反射 + SPI) =================
            // Flyway Plugin SPI 接口（ServiceLoader 加载入口）
            hints.reflection().registerType(TypeReference.of("org.flywaydb.core.extensibility.Plugin"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // Flyway 核心数据库类型（通过 SPI 反射实例化）
            String[] flywayDatabaseTypes = new String[]{
                    "org.flywaydb.database.mysql.MySQLDatabaseType",              // flyway-mysql 模块
                    "org.flywaydb.database.mysql.MySQLConnection",
                    "org.flywaydb.database.mysql.MySQLParser",
                    "org.flywaydb.core.internal.database.base.BaseDatabaseType",
            };
            for (String dbType : flywayDatabaseTypes) {
                hints.reflection().registerType(TypeReference.of(dbType),
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.ACCESS_DECLARED_FIELDS);
            }

            // Flyway 核心执行器和扫描器（native 下 classpath 扫描需反射）
            String[] flywayInternals = new String[]{
                    "org.flywaydb.core.internal.scanner.classpath.ClassPathScanner",
                    "org.flywaydb.core.internal.sqlscript.DefaultSqlScriptExecutor",
                    "org.flywaydb.core.internal.resource.classpath.ClassPathResource",
                    "org.flywaydb.core.internal.jdbc.JdbcTemplate",
                    "org.flywaydb.core.internal.schemahistory.JdbcTableSchemaHistory",
                    "org.flywaydb.core.api.configuration.FluentConfiguration",
            };
            for (String internal : flywayInternals) {
                hints.reflection().registerType(TypeReference.of(internal),
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS);
            }

            // ================= 2.1 自定义 SQLite 日期处理器 =================
            hints.reflection().registerType(TypeReference.of("com.fortuneboot.infrastructure.handler.SqliteDateTypeHandler"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);

            // ================= 2.2 HikariCP（SQLite 模式连接池）=================
            hints.reflection().registerType(TypeReference.of("com.zaxxer.hikari.HikariDataSource"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);

            // ================= 2.3 Druid（MySQL 模式连接池）=================
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.pool.DruidDataSource"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.pool.DruidAbstractDataSource"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.pool.DruidPooledConnection"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            // Druid 监控 Servlet 和 Web 过滤器（手动注册，替代被排除的 DruidDataSourceAutoConfigure）
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.support.jakarta.StatViewServlet"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.support.jakarta.WebStatFilter"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("com.alibaba.druid.wall.WallConfig"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);

            // ================= 2.4 MySQL JDBC 驱动 =================
            hints.reflection().registerType(TypeReference.of("com.mysql.cj.jdbc.Driver"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            hints.reflection().registerType(TypeReference.of("com.mysql.cj.jdbc.ConnectionImpl"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // ================= 3. JWT (JSON Web Token) 相关反射 =================
            // 3.1 SPI 服务文件（jjwt 0.13.0 使用 ServiceLoader 加载实现类，必须注册）
            hints.resources().registerPattern("META-INF/services/io.jsonwebtoken.*");

            // 3.2 ServiceLoader 核心加载机制
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.lang.Services"),
                    MemberCategory.values());

            // 3.3 JWT Builder / Parser 核心实现类
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParser"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtParserBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultJwtHeaderBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // 3.4 Claims / Header Builder
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaimsBuilder"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultHeaderBuilder"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaimsBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultHeaderBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultClaims"),
                    MemberCategory.values());

            // 3.5 Jackson 序列化/反序列化（通过 SPI 加载）
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonSerializer"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.jackson.io.JacksonDeserializer"),
                    MemberCategory.values());

            // 3.6 安全/签名算法（Jwts.SIG / Jwts.KEY / Jwts.ENC 静态初始化需要）
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.KeysBridge"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardKeyOperations"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardKeyAlgorithms"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardCurves"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.StandardHashAlgorithms"),
                    MemberCategory.values());

            // 3.7 security $Supplier 类（通过 Class.forName 动态加载，初始化链必须）
            // 错误链：Jwts.builder() → DefaultJwtBuilder → DefaultProtectedHeader → AbstractJwk
            //       → KeyOperationConverter → Jwks$OP → DefaultKeyOperationBuilder$Supplier
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.DefaultKeyOperationBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.DefaultKeyOperationPolicyBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.DefaultDynamicJwkBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.DefaultJwkSetBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.DefaultJwkParserBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.DefaultJwkSetParserBuilder$Supplier"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

            // 3.8 初始化链中用到的关键内部类
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.KeyOperationConverter"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.security.AbstractJwk"),
                    MemberCategory.values());
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.DefaultProtectedHeader"),
                    MemberCategory.values());

            // 3.9 压缩算法（0.13.0 类名从 *Codec 改为 *Algorithm）
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.DeflateCompressionAlgorithm"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.compression.GzipCompressionAlgorithm"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);
            hints.reflection().registerType(TypeReference.of("io.jsonwebtoken.impl.io.StandardCompressionAlgorithms"),
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_PUBLIC_METHODS);

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