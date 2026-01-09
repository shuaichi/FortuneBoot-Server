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
@EnableScheduling
@AllArgsConstructor
@Configuration(proxyBeanMethods = false)
public class QuartzConfig {

    private final QuartzJobFactory quartzJobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        // 支持 Spring 注入 Job
        factory.setJobFactory(quartzJobFactory);
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }
}
