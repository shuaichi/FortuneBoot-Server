package com.fortuneboot.service.fortune.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 单据审计JSON序列化器。
 * <p>使用Spring统一ObjectMapper，保证审计快照与幂等回执的序列化配置一致。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderAuditJsonSerializer {

    private final ObjectMapper objectMapper;

    public String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("单据审计JSON序列化失败", exception);
        }
    }
}
