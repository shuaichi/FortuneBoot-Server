package com.fortuneboot.common.serializer;

import cn.hutool.core.lang.Pair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/9 13:53
 **/
public class CategoryAmountPairSerializer extends JsonSerializer<List<Pair<Long, BigDecimal>>> {
    @Override
    public void serialize(List<Pair<Long, BigDecimal>> pairList, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        // 开始数组
        gen.writeStartArray();
        for (Pair<Long, BigDecimal> pair : pairList) {
            // 开始一个对象
            gen.writeStartObject();
            // 写入 "categoryId" 字段
            gen.writeObjectField("categoryId", pair.getKey());
            // 写入 "amount" 字段
            gen.writeObjectField("amount", pair.getValue());
            // 结束当前对象
            gen.writeEndObject();
        }
        // 结束数组
        gen.writeEndArray();
    }
}

