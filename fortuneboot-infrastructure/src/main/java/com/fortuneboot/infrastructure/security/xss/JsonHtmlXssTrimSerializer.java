package com.fortuneboot.infrastructure.security.xss;

import cn.hutool.http.HtmlUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;


/**
 * 直接将html标签去掉
 * @author valarchie
 */
public class JsonHtmlXssTrimSerializer extends ValueDeserializer<String> {

    public JsonHtmlXssTrimSerializer() {
        super();
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if( value != null) {
            // 去除掉html标签    如果想要转义的话  可使用 HtmlUtil.escape()
            return HtmlUtil.cleanHtmlTag(value);
        }
        return null;
    }

    @Override
    public Class<String> handledType() {
        return String.class;
    }

}
