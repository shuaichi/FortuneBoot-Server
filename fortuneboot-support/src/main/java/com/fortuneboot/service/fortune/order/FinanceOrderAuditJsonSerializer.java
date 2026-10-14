package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.utils.jackson.JacksonUtil;
import org.springframework.stereotype.Component;

/**
 * 单据审计JSON序列化器。
 * <p>复用统一的JacksonUtil，保证审计快照与幂等回执的序列化配置一致。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
public class FinanceOrderAuditJsonSerializer {

    public String write(Object value) {
        try {
            return JacksonUtil.to(value);
        } catch (Exception exception) {
            throw new IllegalStateException("单据审计JSON序列化失败", exception);
        }
    }
}
