package com.fortuneboot.infrastructure.config.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author zhangchi118
 * @date 2025/7/4 18:07
 **/
@Component
public class QuartzJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

    private transient AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Class<?> jobClass = bundle.getJobDetail().getJobClass();
        // 直接从Spring容器获取Bean实例
        return beanFactory.getBean(jobClass);
    }
}