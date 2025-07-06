package com.fortuneboot.infrastructure.config.quartz;

import lombok.AllArgsConstructor;
import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * @author zhangchi118
 * @date 2025/7/3 19:27
 **/
@Configuration
@EnableScheduling
@AllArgsConstructor
public class QuartzConfig {

    private QuartzJobFactory quartzJobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(Boolean.TRUE);
        // 设置自定义JobFactory，支持Spring依赖注入
        factory.setJobFactory(quartzJobFactory);
        // 延迟60秒启动
        factory.setStartupDelay(60);
        return factory;
    }

    @Bean
    public Scheduler scheduler() {
        return schedulerFactoryBean().getScheduler();
    }
}
