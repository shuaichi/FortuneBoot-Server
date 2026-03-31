package com.fortuneboot.infrastructure.annotations.unrepeatable;

import cn.hutool.cache.impl.LRUCache;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import java.lang.reflect.Type;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * 重复提交拦截器 如果涉及前后端加解密的话  也可以通过继承RequestBodyAdvice来实现
 * 使用内存 LRU 缓存实现防重复提交
 *
 * @author valarchie
 */
@ControllerAdvice(basePackages = "com.fortuneboot")
@Slf4j
@RequiredArgsConstructor
public class UnrepeatableInterceptor extends RequestBodyAdviceAdapter {

    /**
     * 内存缓存：key -> 上次请求体内容，最大4096个key，超时自动淘汰
     */
    private final LRUCache<String, String> resubmitCache = new LRUCache<>(4096, 10 * 1000L);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(Unrepeatable.class);
    }

    /**
     * @param body 仅获取有RequestBody注解的参数
     */
    @NotNull
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
        Class<? extends HttpMessageConverter<?>> converterType) {
        // 仅获取有RequestBody注解的参数
        String currentRequest = JacksonUtil.to(body);

        Unrepeatable resubmitAnno = parameter.getMethodAnnotation(Unrepeatable.class);
        if (resubmitAnno != null) {
            String cacheKey = resubmitAnno.checkType().generateResubmitCacheKey(parameter.getMethod());

            log.info("请求重复提交拦截，当前key:{}, 当前参数：{}", cacheKey, currentRequest);

            String preRequest = resubmitCache.get(cacheKey);
            if (preRequest != null) {
                boolean isSameRequest = Objects.equals(currentRequest, preRequest);

                if (isSameRequest) {
                    throw new ApiException(ErrorCode.Client.COMMON_REQUEST_RESUBMIT);
                }
            }
            // interval 单位为秒，LRUCache 超时在构造时已设置为默认值，这里直接放入
            resubmitCache.put(cacheKey, currentRequest, resubmitAnno.interval() * 1000L);
        }

        return body;
    }

}