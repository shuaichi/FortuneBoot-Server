package com.fortuneboot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置类
 *
 * @author zhangchi118
 * @date 2026/3/13 13:29
 **/
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    public static final String ASYNC_EXECUTOR_NAME = "asyncTaskExecutor";

    @Bean(name = ASYNC_EXECUTOR_NAME)
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：根据实际业务峰值调整
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(50);
        // 队列容量：缓冲执行任务的队列
        executor.setQueueCapacity(200);
        // 线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 线程名前缀，便于日志排查
        executor.setThreadNamePrefix("async-task-");
        // 拒绝策略：当线程池和队列都满时，由调用线程（提交任务的线程）直接执行该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        log.info("异步线程池 [{}] 初始化完成", ASYNC_EXECUTOR_NAME);
        return executor;
    }
}
