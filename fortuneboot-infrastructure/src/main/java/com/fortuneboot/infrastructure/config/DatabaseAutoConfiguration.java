package com.fortuneboot.infrastructure.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

/**
 * 数据源自动配置，根据 db.type 环境变量切换 MySQL / SQLite
 * <p>
 * 注意：不使用 @ConditionalOnProperty，因为在 GraalVM native image 的 AOT 编译阶段
 * 条件注解会被提前求值并固化，导致运行时无法通过环境变量切换数据源。
 * 改为在运行时根据 db.type 动态创建对应的 DataSource。
 * <p>
 * 同理，DruidDataSourceAutoConfigure 也被排除（见 ApplicationStarter），
 * 因此 Druid 的过滤器（stat/wall）和监控（StatViewServlet/WebStatFilter）
 * 需要在此类中手动配置。
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

    // ===================== Druid 监控：StatViewServlet =====================

    /**
     * 注册 Druid 监控控制台 Servlet（/druid/*）
     * 非 MySQL 模式下自动禁用
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet(
            @Value("${db.type:sqlite}") String dbType,
            Environment env) {
        ServletRegistrationBean<StatViewServlet> reg = new ServletRegistrationBean<>();
        if (!"mysql".equalsIgnoreCase(dbType)) {
            reg.setEnabled(false);
            return reg;
        }
        reg.setServlet(new StatViewServlet());
        reg.addUrlMappings(env.getProperty("spring.datasource.druid.statViewServlet.url-pattern", "/druid/*"));

        String loginUsername = env.getProperty("spring.datasource.druid.statViewServlet.login-username", "admin");
        String loginPassword = env.getProperty("spring.datasource.druid.statViewServlet.login-password", "admin");
        reg.addInitParameter("loginUsername", loginUsername);
        reg.addInitParameter("loginPassword", loginPassword);

        String allow = env.getProperty("spring.datasource.druid.statViewServlet.allow");
        if (allow != null && !allow.isEmpty()) {
            reg.addInitParameter("allow", allow);
        }
        log.info(">>> 注册 Druid StatViewServlet, loginUsername={}", loginUsername);
        return reg;
    }

    // ===================== Druid 监控：WebStatFilter =====================

    /**
     * 注册 Druid Web 监控过滤器（统计 Web 请求的 SQL 信息）
     * 非 MySQL 模式下自动禁用
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter(
            @Value("${db.type:sqlite}") String dbType) {
        FilterRegistrationBean<WebStatFilter> reg = new FilterRegistrationBean<>();
        if (!"mysql".equalsIgnoreCase(dbType)) {
            reg.setEnabled(false);
            return reg;
        }
        reg.setFilter(new WebStatFilter());
        reg.addUrlPatterns("/*");
        reg.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        log.info(">>> 注册 Druid WebStatFilter");
        return reg;
    }

    // ===================== MySQL 数据源 =====================

    /**
     * MySQL 数据源（使用 Druid 连接池）
     * 注意：不使用 Binder 绑定，因为 Binder 依赖反射在 GraalVM native image 中会失败
     */
    private DataSource createMysqlDataSource(Environment environment) {
        log.info(">>> 初始化 MySQL 数据源 (Druid)");
        DruidDataSource dataSource = new DruidDataSource();

        // 基础连接属性
        dataSource.setUrl(environment.getProperty("spring.datasource.druid.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.druid.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.druid.password"));
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 连接池参数
        dataSource.setInitialSize(getIntProperty(environment, "spring.datasource.druid.initialSize", 5));
        dataSource.setMinIdle(getIntProperty(environment, "spring.datasource.druid.minIdle", 10));
        dataSource.setMaxActive(getIntProperty(environment, "spring.datasource.druid.maxActive", 20));
        dataSource.setMaxWait(getLongProperty(environment, "spring.datasource.druid.maxWait", 60000L));
        dataSource.setTimeBetweenEvictionRunsMillis(
                getLongProperty(environment, "spring.datasource.druid.timeBetweenEvictionRunsMillis", 60000L));
        dataSource.setMinEvictableIdleTimeMillis(
                getLongProperty(environment, "spring.datasource.druid.minEvictableIdleTimeMillis", 300000L));
        dataSource.setMaxEvictableIdleTimeMillis(
                getLongProperty(environment, "spring.datasource.druid.maxEvictableIdleTimeMillis", 900000L));
        dataSource.setValidationQuery(
                environment.getProperty("spring.datasource.druid.validationQuery", "SELECT 1 FROM DUAL"));
        dataSource.setTestWhileIdle(
                Boolean.parseBoolean(environment.getProperty("spring.datasource.druid.testWhileIdle", "true")));
        dataSource.setTestOnBorrow(
                Boolean.parseBoolean(environment.getProperty("spring.datasource.druid.testOnBorrow", "false")));
        dataSource.setTestOnReturn(
                Boolean.parseBoolean(environment.getProperty("spring.datasource.druid.testOnReturn", "false")));

        // Druid 过滤器（stat 慢SQL统计 + wall SQL防火墙）
        configureDruidFilters(dataSource, environment);

        return dataSource;
    }

    /**
     * 手动配置 Druid 过滤器（替代被排除的 DruidDataSourceAutoConfigure）
     */
    private void configureDruidFilters(DruidDataSource dataSource, Environment environment) {
        // Stat 过滤器：慢 SQL 记录
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(
                Boolean.parseBoolean(environment.getProperty("spring.datasource.druid.filter.stat.log-slow-sql", "true")));
        statFilter.setSlowSqlMillis(
                getLongProperty(environment, "spring.datasource.druid.filter.stat.slow-sql-millis", 1000L));
        statFilter.setMergeSql(
                Boolean.parseBoolean(environment.getProperty("spring.datasource.druid.filter.stat.merge-sql", "true")));

        // Wall 过滤器：SQL 防火墙
        WallFilter wallFilter = new WallFilter();
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(
                Boolean.parseBoolean(environment.getProperty(
                        "spring.datasource.druid.filter.wall.config.multi-statement-allow", "true")));
        wallFilter.setConfig(wallConfig);

        dataSource.setProxyFilters(List.of(statFilter, wallFilter));
        log.info(">>> 配置 Druid 过滤器: stat(slowSqlMillis={}), wall(multiStatementAllow={})",
                statFilter.getSlowSqlMillis(), wallConfig.isMultiStatementAllow());
    }

    // ===================== SQLite 数据源 =====================

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

    // ===================== 工具方法 =====================

    private int getIntProperty(Environment env, String key, int defaultValue) {
        String value = env.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    private long getLongProperty(Environment env, String key, long defaultValue) {
        String value = env.getProperty(key);
        return value != null ? Long.parseLong(value) : defaultValue;
    }
}