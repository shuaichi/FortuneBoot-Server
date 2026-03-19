package com.fortuneboot.listener;

import com.fortuneboot.config.AsyncConfig;
import com.fortuneboot.domain.command.fortune.FortuneGroupAddCommand;
import com.fortuneboot.domain.event.UserRegisteredEvent;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.service.fortune.FortuneGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final FortuneGroupService fortuneGroupService;

    @Async(AsyncConfig.ASYNC_EXECUTOR_NAME)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegisteredInitData(UserRegisteredEvent event) {
        log.info("接收到用户注册成功事件，开始异步初始化数据。用户ID: {}", event.getUserId());

        // 【修复：审计字段为空】手动构造 Security 上下文，防止 MyBatis Plus 自动填充创建人时报错/为 null
        SystemLoginUser mockUser = new SystemLoginUser();
        mockUser.setUserId(event.getUserId());
        mockUser.setUsername(event.getUsername());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(mockUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        try {
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
        } finally {
            // 清理上下文避免线程池污染
            SecurityContextHolder.clearContext();
        }
    }
}