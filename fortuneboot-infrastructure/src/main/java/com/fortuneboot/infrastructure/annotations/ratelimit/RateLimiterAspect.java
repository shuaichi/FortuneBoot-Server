package com.fortuneboot.infrastructure.annotations.ratelimit;

import com.fortuneboot.infrastructure.annotations.ratelimit.implementation.MapRateLimitChecker;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 限流切面处理（统一使用内存 Map 限流）
 *
 * @author valarchie
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final MapRateLimitChecker mapRateLimitChecker;

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimit rateLimiter) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        log.info("当前限流方法:" + method.toGenericString());

        mapRateLimitChecker.check(rateLimiter);
    }

}