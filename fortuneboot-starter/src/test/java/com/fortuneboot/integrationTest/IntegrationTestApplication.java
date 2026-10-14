package com.fortuneboot.integrationTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import com.fortuneboot.admin.ApplicationStarter;
import com.fortuneboot.orderapi.FinanceOrderStage03TestApplication;
import com.fortuneboot.orderapi.FinanceOrderStage04TestApplication;
import com.fortuneboot.service.fortune.order.FinanceOrderSqliteTestApplication;
import org.springframework.context.annotation.FilterType;

/**
 * 集成测试配置类
 * @author valarchie
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
// 排除生产启动类与单据域测试上下文：ApplicationStarter 自带无排除的
// com.fortuneboot.* 扫描，会把 orderapi 下自带独立 dataSource Bean 的
// Stage03/04 测试应用再次扫进来，触发 BeanDefinitionOverrideException；
// 这些上下文只服务各自的 HTTP 契约测试，不属于本集成测试装配范围。
@ComponentScan(basePackages = "com.fortuneboot.*",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {ApplicationStarter.class,
                        FinanceOrderStage03TestApplication.class,
                        FinanceOrderStage04TestApplication.class,
                        FinanceOrderSqliteTestApplication.class}))
public class IntegrationTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestApplication.class, args);
    }
}
