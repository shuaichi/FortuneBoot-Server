package com.fortuneboot.infrastructure.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.File;

/**
 * 数据源自动配置，根据 db.type 环境变量切换 MySQL / SQLite
 *
 * @author fortuneboot
 */
@Configuration
public class DatabaseAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseAutoConfiguration.class);

    /**
     * MySQL 数据源（使用 Druid 连接池）
     * 当 db.type=mysql 时激活
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "db.type", havingValue = "mysql")
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource mysqlDataSource() {
        log.info(">>> 初始化 MySQL 数据源 (Druid)");
        DruidDataSource dataSource = new DruidDataSource();
        return dataSource;
    }

    /**
     * SQLite 数据源（使用 HikariCP 连接池）
     * 当 db.type=sqlite 或未设置时激活（默认模式）
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "db.type", havingValue = "sqlite", matchIfMissing = true)
    public DataSource sqliteDataSource(@Value("${db.sqlite.path:/data/fortuneboot.db}") String dbPath) {
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