package com.fortuneboot.common.serializer;

import cn.hutool.core.lang.Pair;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/9 13:52
 **/
public class CategoryAmountPairDeserializer extends JsonDeserializer<List<Pair<Long, BigDecimal>>> {
    @Override
    public List<Pair<Long, BigDecimal>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        List<Pair<Long, BigDecimal>> list = new ArrayList<>();
        if (node.isArray() && !node.isEmpty()) {
            for (JsonNode item : node){
                Long key = item.get("categoryId").asLong();
                BigDecimal value = new BigDecimal(item.get("amount").asText());
                Pair<Long, BigDecimal> pair = new Pair<>(key, value);
                list.add(pair);
            }
        }
        return list;
    }
}
