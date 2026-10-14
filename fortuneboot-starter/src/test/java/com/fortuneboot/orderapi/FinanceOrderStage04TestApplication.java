package com.fortuneboot.orderapi;

import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.infrastructure.config.ApplicationConfig;
import com.fortuneboot.infrastructure.config.MyBatisConfig;
import com.fortuneboot.infrastructure.exception.GlobalExceptionInterceptor;
import com.fortuneboot.infrastructure.mybatisplus.CustomMetaObjectHandler;
import com.fortuneboot.rest.fortune.FortuneBillRest;
import com.fortuneboot.rest.fortune.FortuneFinanceOrderV2Rest;
import com.fortuneboot.service.permission.FortunePermissionService;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.SpringApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 阶段04测试专用上下文。
 * <p>在既有单据管线外补齐统计、导入、相邻服务和V2导出REST，用于真实SQLite验证。
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@EnableMethodSecurity
@ComponentScan(basePackages = {
        "com.fortuneboot.service.fortune",
        "com.fortuneboot.strategy.bill",
        "com.fortuneboot.factory.fortune",
        "com.fortuneboot.repository.fortune.impl",
        "com.fortuneboot.repository.system.impl"},
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.fortuneboot.service.fortune.order.FinanceOrderSqliteTestApplication.class))
@Import({ApplicationConfig.class, MyBatisConfig.class, ApplicationScopeBo.class,
        FortunePermissionService.class, GlobalExceptionInterceptor.class, CustomMetaObjectHandler.class,
        FortuneFinanceOrderV2Rest.class, FortuneBillRest.class})
@MapperScan(value = {"com.fortuneboot.dao.fortune", "com.fortuneboot.dao.system"},
        markerInterface = com.baomidou.mybatisplus.core.mapper.BaseMapper.class,
        sqlSessionTemplateRef = "sqlSessionTemplate")
public class FinanceOrderStage04TestApplication {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = SpringApplication.run(FinanceOrderStage04TestApplication.class, args)) {
            // 测试上下文入口仅用于本地启动检查。
        }
    }

    @Bean
    @ConditionalOnProperty(name = "finance-order.test.db-type", havingValue = "sqlite", matchIfMissing = true)
    DataSource dataSource(@Value("${finance-order.test.sqlite-url}") String url) {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setEnforceForeignKeys(false);
        return dataSource;
    }

    @Bean
    @ConditionalOnProperty(name = "finance-order.test.db-type", havingValue = "mysql")
    DataSource mysqlDataSource(@Value("${finance-order.test.mysql-url}") String url,
                               @Value("${finance-order.test.mysql-user}") String user,
                               @Value("${finance-order.test.mysql-password}") String password) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean
    @Primary
    Clock testClock() {
        return Clock.fixed(LocalDate.of(2026, 10, 8).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant(),
                ZoneId.of("Asia/Shanghai"));
    }

}
