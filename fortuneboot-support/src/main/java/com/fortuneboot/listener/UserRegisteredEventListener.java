package com.fortuneboot.listener;

import com.fortuneboot.config.AsyncConfig;
import com.fortuneboot.domain.command.fortune.FortuneGroupAddCommand;
import com.fortuneboot.domain.event.UserRegisteredEvent;
import com.fortuneboot.service.fortune.FortuneGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2026/3/13 13:26
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final FortuneGroupService fortuneGroupService;

    /**
     * 监听用户注册成功事件，异步初始化用户的默认分组与账本
     *
     * @Async 指定使用我们配置的自定义线程池
     * @TransactionalEventListener 指定在发布事件的方法所在事务 提交之后 (AFTER_COMMIT) 执行
     */
    @Async(AsyncConfig.ASYNC_EXECUTOR_NAME)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegisteredInitData(UserRegisteredEvent event) {
        log.info("接收到用户注册成功事件，开始异步初始化数据。用户ID: {}", event.getUserId());

        try {
            // 原有的同步初始化逻辑
            FortuneGroupAddCommand groupAddCommand = new FortuneGroupAddCommand();
            groupAddCommand.setGroupName(Objects.requireNonNullElse(event.getNickname(), event.getUsername()));
            groupAddCommand.setDefaultCurrency("CNY");
            groupAddCommand.setEnable(Boolean.TRUE);
            groupAddCommand.setBookTemplate(1L);
            groupAddCommand.setRemark("新建账户创建的默认分组。");

            fortuneGroupService.add(groupAddCommand, event.getUserId());

            log.info("用户ID: {} 的默认分组与账本初始化成功", event.getUserId());
        } catch (Exception e) {
            // 异常捕获必须完善，避免抛出到 Spring 框架层面
            // 异步任务的异常不会影响主流程，但我们需要日志记录以便后续通过定时任务或人工介入补偿处理
            log.error("用户ID: {} 异步初始化数据失败，原因: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
