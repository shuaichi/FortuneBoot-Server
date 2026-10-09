package com.fortuneboot.service.fortune.order;

import com.fortuneboot.infrastructure.config.ApplicationConfig;
import com.fortuneboot.infrastructure.config.MyBatisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 阶段02测试专用SQLite/MySQL上下文。
 * <p>装配统一写入管线、旧BillService、策略、工厂和Repository；排除依赖系统仓库且与本阶段无关的服务。
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.fortuneboot.service.fortune",
                "com.fortuneboot.strategy.bill",
                "com.fortuneboot.factory.fortune",
                "com.fortuneboot.repository.fortune.impl"},
        excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = {com.fortuneboot.service.fortune.FortuneGroupService.class,
                        com.fortuneboot.service.fortune.FortuneUserGroupRelationService.class,
                        com.fortuneboot.service.fortune.FortuneIncludeService.class}))
@Import({ApplicationConfig.class, MyBatisConfig.class, ApplicationScopeBo.class})
@MapperScan(value = "com.fortuneboot.dao.fortune",
        markerInterface = com.baomidou.mybatisplus.core.mapper.BaseMapper.class,
        sqlSessionTemplateRef = "sqlSessionTemplate")
public class FinanceOrderSqliteTestApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(FinanceOrderSqliteTestApplication.class, args);
        context.close();
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

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
