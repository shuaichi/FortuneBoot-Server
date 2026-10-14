package com.fortuneboot.service.fortune.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据审计JSON序列化器")
class FinanceOrderAuditJsonSerializerTest {

    private final FinanceOrderAuditJsonSerializer serializer =
            new FinanceOrderAuditJsonSerializer();

    @Test
    @DisplayName("按LinkedMap顺序稳定序列化审计快照")
    void write_serializesAuditSnapshot() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", 1L);
        value.put("amount", "100.00");

        String json = serializer.write(value);

        assertThat(json).isEqualTo("{\"orderId\":1,\"amount\":\"100.00\"}");
    }

    @Test
    @DisplayName("序列化失败包装为IllegalStateException并保留原因")
    void write_wrapsSerializationFailure() {
        assertThatThrownBy(() -> serializer.write(new FailingSnapshot()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("单据审计JSON序列化失败")
                .hasCauseInstanceOf(Exception.class);
    }

    private static class FailingSnapshot {
        public long getOrderId() {
            throw new IllegalStateException("boom");
        }
    }
}
