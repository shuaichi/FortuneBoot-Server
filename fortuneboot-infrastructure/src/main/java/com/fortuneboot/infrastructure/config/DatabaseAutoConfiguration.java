package com.fortuneboot.infrastructure.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.File;

/**
 * 数据源自动配置，根据 db.type 环境变量切换 MySQL / SQLite
 * <p>
 * 注意：不使用 @ConditionalOnProperty，因为在 GraalVM native image 的 AOT 编译阶段
 * 条件注解会被提前求值并固化，导致运行时无法通过环境变量切换数据源。
 * 改为在运行时根据 db.type 动态创建对应的 DataSource。
 *
 * @author fortuneboot
 */
@Configuration
public class DatabaseAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseAutoConfiguration.class);

    @Bean
    @Primary
    public DataSource dataSource(
            Environment environment,
            @Value("${db.type:sqlite}") String dbType,
            @Value("${db.sqlite.path:/data/fortuneboot.db}") String sqlitePath) {

        if ("mysql".equalsIgnoreCase(dbType)) {
            return createMysqlDataSource(environment);
        } else {
            return createSqliteDataSource(sqlitePath);
        }
    }

    /**
     * MySQL 数据源（使用 Druid 连接池）
     */
    private DataSource createMysqlDataSource(Environment environment) {
        log.info(">>> 初始化 MySQL 数据源 (Druid)");
        DruidDataSource dataSource = new DruidDataSource();
        // 通过 Binder 绑定 spring.datasource.druid.* 配置
        Binder binder = Binder.get(environment);
        binder.bind("spring.datasource.druid", DruidDataSource.class)
                .ifBound(props -> {
                    dataSource.setUrl(props.getUrl());
                    dataSource.setUsername(props.getUsername());
                    dataSource.setPassword(props.getPassword());
                    dataSource.setInitialSize(props.getInitialSize());
                    dataSource.setMinIdle(props.getMinIdle());
                    dataSource.setMaxActive(props.getMaxActive());
                    dataSource.setMaxWait(props.getMaxWait());
                    dataSource.setTimeBetweenEvictionRunsMillis(props.getTimeBetweenEvictionRunsMillis());
                    dataSource.setMinEvictableIdleTimeMillis(props.getMinEvictableIdleTimeMillis());
                    dataSource.setMaxEvictableIdleTimeMillis(props.getMaxEvictableIdleTimeMillis());
                    dataSource.setValidationQuery(props.getValidationQuery());
                    dataSource.setTestWhileIdle(props.isTestWhileIdle());
                    dataSource.setTestOnBorrow(props.isTestOnBorrow());
                    dataSource.setTestOnReturn(props.isTestOnReturn());
                });
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    /**
     * SQLite 数据源（使用 HikariCP 连接池）
     */
    private DataSource createSqliteDataSource(String dbPath) {
        log.info(">>> 初始化 SQLite 数据源, 数据文件: {}", dbPath);

        // 确保数据文件的父目录存在
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                log.info(">>> 创建数据目录: {}", parentDir.getAbsolutePath());
            }
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:sqlite:" + dbPath + "?date_string_format=yyyy-MM-dd HH:mm:ss");
        dataSource.setDriverClassName("org.sqlite.JDBC");
        // SQLite 单写连接，避免并发写入死锁
        dataSource.setMaximumPoolSize(1);
        dataSource.setMinimumIdle(1);
        // 启用 WAL 模式、外键约束和忙等待超时
        dataSource.setConnectionInitSql(
            "PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON; PRAGMA busy_timeout=5000;"
        );
        return dataSource;
    }
}