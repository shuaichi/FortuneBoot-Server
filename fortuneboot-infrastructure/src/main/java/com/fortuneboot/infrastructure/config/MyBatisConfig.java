package com.fortuneboot.infrastructure.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fortuneboot.infrastructure.handler.SqliteBlobTypeHandler;
import com.fortuneboot.infrastructure.handler.SqliteDateTypeHandler;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Date;
import java.util.Properties;

/**
 * Mybatis支持*匹配扫描包
 *
 * @author valarchie
 */
@Configuration
@EnableTransactionManagement
public class MyBatisConfig {

    private static final Logger log = LoggerFactory.getLogger(MyBatisConfig.class);

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(
            @Value("${db.type:sqlite}") String dbType) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DbType type = DbType.MYSQL.getDb().equals(dbType) ? DbType.MYSQL : DbType.SQLITE;
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(type));
        return interceptor;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

    /**
     * 通过 ConfigurationCustomizer 设置 MyBatis databaseId，
     * 并在 SQLite 模式下注册自定义日期处理器
     */
    @Bean
    public ConfigurationCustomizer databaseIdCustomizer(
            @Value("${db.type:sqlite}") String dbType) {
        return configuration -> {
            log.info(">>> ConfigurationCustomizer 设置 MyBatis databaseId: {}", dbType);
            configuration.setDatabaseId(dbType);

            // SQLite 模式：注册自定义日期类型处理器，替代默认的 DateTypeHandler
            // 确保日期以 'yyyy-MM-dd HH:mm:ss' 字符串格式存储/读取
            if (DbType.SQLITE.getDb().equals(dbType)) {
                log.info(">>> SQLite 模式：注册 SqliteDateTypeHandler");
                SqliteDateTypeHandler handler = new SqliteDateTypeHandler();
                configuration.getTypeHandlerRegistry().register(Date.class, handler);
                configuration.getTypeHandlerRegistry().register(Date.class, JdbcType.TIMESTAMP, handler);
                configuration.getTypeHandlerRegistry().register(Date.class, JdbcType.DATE, handler);
                configuration.getTypeHandlerRegistry().register(Date.class, JdbcType.TIME, handler);

                log.info(">>> SQLite 模式：注册 SqliteBlobTypeHandler");
                SqliteBlobTypeHandler blobHandler = new SqliteBlobTypeHandler();
                configuration.getTypeHandlerRegistry().register(byte[].class, blobHandler);
                configuration.getTypeHandlerRegistry().register(byte[].class, JdbcType.BLOB, blobHandler);
                configuration.getTypeHandlerRegistry().register(byte[].class, JdbcType.BINARY, blobHandler);
                configuration.getTypeHandlerRegistry().register(byte[].class, JdbcType.VARBINARY, blobHandler);
                configuration.getTypeHandlerRegistry().register(byte[].class, JdbcType.LONGVARBINARY, blobHandler);
            }
        };
    }

    /**
     * MyBatis 数据库方言识别（通过 ObjectProvider 延迟注入）
     * 根据 JDBC 连接自动识别 databaseId（mysql / sqlite）
     */
    @Bean
    public VendorDatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("MySQL", DbType.MYSQL.getDb());
        properties.setProperty("SQLite", DbType.SQLITE.getDb());
        provider.setProperties(properties);
        return provider;
    }
}