package com.fortuneboot.infrastructure.config;

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.resource.LoadableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Flyway 数据库迁移配置
 * <p>
 * 根据 db.type 自动选择对应的迁移脚本目录：
 * - mysql: db/migration/mysql (V1.0.0 基线 + V1.1.0~V1.5.0 增量链)
 * - sqlite: db/migration/sqlite (V1.5.0 全量)
 * <p>
 * 对于已有的 MySQL 数据库（无 flyway_schema_history），
 * 通过表/列存在性智能检测当前版本并设置 baseline。
 * <p>
 * 注意：在 GraalVM native image 环境下，Flyway 内置的 ClassPathScanner 无法通过
 * 目录枚举发现 SQL 文件。因此使用 Spring 的 PathMatchingResourcePatternResolver
 * 显式解析 SQL 资源，并通过自定义 ResourceProvider 提供给 Flyway。
 *
 * @author fortuneboot
 */
@Configuration
public class FlywayConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfiguration.class);

    @Bean
    public Flyway flyway(DataSource dataSource,
                         @Value("${db.type:sqlite}") String dbType) {
        String locationPath = "db/migration/" + dbType;
        log.info(">>> Flyway 迁移脚本目录: classpath:{}", locationPath);

        String baselineVersion = detectBaselineVersion(dataSource, dbType);
        log.info(">>> Flyway baseline 版本: {}", baselineVersion);

        // 使用 Spring ResourcePatternResolver 显式枚举 SQL 文件
        // 解决 GraalVM native image 下 Flyway ClassPathScanner 无法枚举目录的问题
        ResourceProvider resourceProvider = buildSpringResourceProvider(locationPath);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .resourceProvider(resourceProvider)
                .baselineOnMigrate(true)
                .baselineVersion(baselineVersion)
                .validateOnMigrate(true)
                .outOfOrder(false)
                .table("flyway_schema_history")
                .load();

        flyway.migrate();
        log.info(">>> Flyway 数据库迁移完成");
        return flyway;
    }

    /**
     * 使用 Spring 的 PathMatchingResourcePatternResolver 枚举 SQL 文件，
     * 构建 Flyway 自定义 ResourceProvider。
     * <p>
     * Spring 的资源解析器能正确配合 AOT resource hints 工作，
     * 在 native image 中也能发现被注册的资源文件。
     */
    private ResourceProvider buildSpringResourceProvider(String locationPath) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<LoadableResource> loadableResources = new ArrayList<>();

        try {
            Resource[] resources = resolver.getResources("classpath:" + locationPath + "/*.sql");
            log.info(">>> Spring ResourceResolver 发现 {} 个 SQL 迁移文件", resources.length);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                log.info(">>>   - {}", filename);
                loadableResources.add(new SpringLoadableResource(locationPath, resource));
            }
        } catch (IOException e) {
            log.error(">>> 枚举 SQL 迁移文件失败: {}", e.getMessage(), e);
        }

        return new ResourceProvider() {
            @Override
            public LoadableResource getResource(String name) {
                return loadableResources.stream()
                        .filter(r -> r.getFilename().equals(name)
                                || r.getAbsolutePath().equals(name)
                                || r.getRelativePath().equals(name))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public Collection<LoadableResource> getResources(String prefix, String[] suffixes) {
                return loadableResources.stream()
                        .filter(r -> {
                            String filename = r.getFilename();
                            if (!filename.startsWith(prefix)) {
                                return false;
                            }
                            for (String suffix : suffixes) {
                                if (filename.endsWith(suffix)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .map(r -> (LoadableResource) r)
                        .toList();
            }
        };
    }

    /**
     * 将 Spring Resource 适配为 Flyway LoadableResource
     */
    private static class SpringLoadableResource extends LoadableResource {
        private final String locationPath;
        private final Resource resource;

        SpringLoadableResource(String locationPath, Resource resource) {
            this.locationPath = locationPath;
            this.resource = resource;
        }

        @Override
        public Reader read() {
            try {
                return new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("无法读取迁移脚本: " + getFilename(), e);
            }
        }

        @Override
        public String getAbsolutePath() {
            return locationPath + "/" + getFilename();
        }

        @Override
        public String getRelativePath() {
            return getAbsolutePath();
        }

        @Override
        public String getAbsolutePathOnDisk() {
            return StringUtils.EMPTY;
        }

        @Override
        public String getFilename() {
            return resource.getFilename();
        }
    }

    /**
     * 智能检测数据库当前版本，确定 baseline 版本号。
     * <p>
     * 检测优先级（从高到低）：
     * - sys_login_token 表存在 → V1.5.0
     * - sys_role.is_admin 列存在 → V1.4.0
     * - fortune_finance_order 表存在 → V1.3.0
     * - fortune_recurring_bill_rule 表存在 → V1.2.0
     * - fortune_goods_keeper 表存在 → V1.1.0
     * - sys_user 表存在（基线数据已有） → V1.0.1
     * - 空数据库 → "0"（从 V1.0.0 开始执行全部脚本）
     */
    private String detectBaselineVersion(DataSource dataSource, String dbType) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            // 已有 flyway_schema_history → Flyway 自行管理，不需要 baseline
            if (hasTable(metaData, "flyway_schema_history")) {
                log.info(">>> 检测到 flyway_schema_history，由 Flyway 自行管理版本");
                return "0";
            }

            // 无业务表 → 全新数据库
            if (!hasTable(metaData, "sys_user")) {
                log.info(">>> 全新数据库，从 V1.0.0 开始迁移");
                return "0";
            }

            // SQLite 只有全量脚本，已有数据库 baseline 到 V1.5.1
            if ("sqlite".equals(dbType)) {
                log.info(">>> SQLite 已有数据库，baseline 到 V1.5.1");
                return "1.5.1";
            }

            // MySQL 已有数据库：按特征从高版本往低版本检测
            if (hasTable(metaData, "sys_login_token")) {
                log.info(">>> 检测到 sys_login_token 表，当前版本 >= V1.5.0");
                return "1.5.0";
            }

            if (hasColumn(metaData, "sys_role", "is_admin")) {
                log.info(">>> 检测到 sys_role.is_admin 列，当前版本 >= V1.4.0");
                return "1.4.0";
            }

            if (hasTable(metaData, "fortune_finance_order")) {
                log.info(">>> 检测到 fortune_finance_order 表，当前版本 >= V1.3.0");
                return "1.3.0";
            }

            if (hasTable(metaData, "fortune_recurring_bill_rule")) {
                log.info(">>> 检测到 fortune_recurring_bill_rule 表，当前版本 >= V1.2.0");
                return "1.2.0";
            }

            if (hasTable(metaData, "fortune_goods_keeper")) {
                log.info(">>> 检测到 fortune_goods_keeper 表，当前版本 >= V1.1.0");
                return "1.1.0";
            }

            // 有 sys_user 但无后续版本特征 → V1.0.1 基线数据已存在
            log.info(">>> 检测到基础业务表，当前版本 = V1.0.1");
            return "1.0.1";

        } catch (Exception e) {
            log.warn(">>> 检测数据库版本时发生异常，将从头开始迁移: {}", e.getMessage());
            return "0";
        }
    }

    /**
     * 检测指定表是否存在
     */
    private boolean hasTable(DatabaseMetaData metaData, String tableName) {
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检测指定表的指定列是否存在
     */
    private boolean hasColumn(DatabaseMetaData metaData, String tableName, String columnName) {
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}