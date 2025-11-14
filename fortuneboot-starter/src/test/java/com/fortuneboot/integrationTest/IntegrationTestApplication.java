package com.fortuneboot.integrationTest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 集成测试配置类
 * @author valarchie
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.fortuneboot.*")
public class IntegrationTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestApplication.class, args);
    }
}
