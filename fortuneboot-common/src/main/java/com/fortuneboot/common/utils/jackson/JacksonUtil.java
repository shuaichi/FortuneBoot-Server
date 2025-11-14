package com.fortuneboot.common.utils.jackson;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.MapType;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Jackson工具类 优势： 数据量高于百万的时候，速度和FastJson相差极小 API和注解支持最完善，可定制性最强
 * 支持的数据源最广泛（字符串，对象，文件、流、URL）
 *
 * @author valarchie
 */
@Slf4j
public class JacksonUtil {

    private static ObjectMapper mapper;

    private static final Set<JsonReadFeature> JSON_READ_FEATURES_ENABLED = CollUtil.newHashSet(
            // 允许在JSON中使用Java注释
            JsonReadFeature.ALLOW_JAVA_COMMENTS,
            // 允许 json 存在没用双引号括起来的 field
            JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES,
            // 允许 json 存在使用单引号括起来的 field
            JsonReadFeature.ALLOW_SINGLE_QUOTES,
            // 允许 json 存在没用引号括起来的 ascii 控制字符
            JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS,
            // 允许 json number 类型的数存在前导 0 (例: 0001)
            JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS,
            // 允许 json 存在 NaN, INF, -INF 作为 number 类型
            JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS,
            // 允许 只有Key没有Value的情况
            JsonReadFeature.ALLOW_MISSING_VALUES,
            // 允许数组json的结尾多逗号
            JsonReadFeature.ALLOW_TRAILING_COMMA
    );

    static {
        try {
            //初始化
            mapper = initMapper();
        } catch (Exception e) {
            log.error("jackson config error", e);
        }
    }

    public static ObjectMapper initMapper() {
        String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
        JsonMapper.Builder builder = JsonMapper.builder()
                .enable(JSON_READ_FEATURES_ENABLED.toArray(new JsonReadFeature[0]))
                .defaultDateFormat(new SimpleDateFormat(dateTimeFormat))
                // 配置序列化级别
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                // 配置JSON缩进支持
                .configure(SerializationFeature.INDENT_OUTPUT, false)
                // 允许单个数值当做数组处理
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                // 禁止重复键, 抛出异常
                .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
                // 禁止使用int代表Enum的order()來反序列化Enum, 抛出异常
                .enable(EnumFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                // 有属性不能映射的时候不报错
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 对象为空时不抛异常
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 时间格式
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 允许未知字段
                .enable(StreamWriteFeature.IGNORE_UNKNOWN)
                // 序列化BigDecimal时之间输出原始数字还是科学计数, 默认false, 即是否以toPlainString()科学计数方式来输出
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN);
        return builder.build();
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(URL url, Class<V> type) {
        try {
            return mapper.readValue(url.openStream(), type);
        } catch (IOException e) {
            throw new JacksonException(StrUtil.format("jackson from error, url: {}, type: {}", url.getPath(), type), e);
        }
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(URL url, TypeReference<V> type) {
        try {
            return mapper.readValue(url.openStream(), type);
        } catch (IOException e) {
            throw new JacksonException(StrUtil.format("jackson from error, url: {}, type: {}", url.getPath(), type), e);
        }
    }

    /**
     * JSON反序列化（List）
     */
    public static <V> List<V> fromList(URL url, Class<V> type) {
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
            return mapper.readValue(url.openStream(), collectionType);
        } catch (IOException e) {
            throw new JacksonException(StrUtil.format("jackson from error, url: {}, type: {}", url.getPath(), type), e);
        }
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(InputStream inputStream, Class<V> type) {
        return mapper.readValue(inputStream, type);
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(InputStream inputStream, TypeReference<V> type) {
        return mapper.readValue(inputStream, type);
    }

    /**
     * JSON反序列化（List）
     */
    public static <V> List<V> fromList(InputStream inputStream, Class<V> type) {
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
        return mapper.readValue(inputStream, collectionType);
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(File file, Class<V> type) {
        return mapper.readValue(file, type);
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(File file, TypeReference<V> type) {
        return mapper.readValue(file, type);
    }

    /**
     * JSON反序列化（List）
     */
    public static <V> List<V> fromList(File file, Class<V> type) {
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
        return mapper.readValue(file, collectionType);
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(String json, Class<V> type) {
        return from(json, (Type) type);
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(String json, TypeReference<V> type) {
        return from(json, type.getType());
    }

    /**
     * JSON反序列化
     */
    public static <V> V from(String json, Type type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return mapper.readValue(json, javaType);
    }

    /**
     * JSON反序列化（List）
     */
    public static <V> List<V> fromList(String json, Class<V> type) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
        return mapper.readValue(json, collectionType);
    }

    /**
     * JSON反序列化（Map）
     */
    public static Map<String, Object> fromMap(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyMap();
        }
        MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        return mapper.readValue(json, mapType);
    }

    /**
     * 序列化为JSON
     */
    public static <V> String to(List<V> list) {
        return mapper.writeValueAsString(list);
    }

    /**
     * 序列化为JSON
     */
    public static <V> String to(V v) {
        return mapper.writeValueAsString(v);
    }

    /**
     * 序列化为JSON
     */
    public static <V> void toFile(String path, List<V> list) {
        try (Writer writer = new FileWriter(path, true)) {
            mapper.writer().writeValues(writer).writeAll(list);
        } catch (Exception e) {
            throw new JacksonException(StrUtil.format("jackson to file error, path: {}, list: {}", path, list), e);
        }
    }

    /**
     * 序列化为JSON
     */
    public static <V> void toFile(String path, V v) {
        try (Writer writer = new FileWriter(path, true)) {
            mapper.writer().writeValues(writer).write(v);
        } catch (IOException e) {
            throw new JacksonException(StrUtil.format("jackson to file error, path: {}, data: {}", path, v), e);
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return String，默认为 null
     */
    public static String getAsString(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return null;
        }
        return getAsString(jsonNode);
    }

    private static String getAsString(JsonNode jsonNode) {
        return jsonNode.isString() ? jsonNode.stringValue() : jsonNode.toString();
    }

    /**
     * 从json串中获取某个字段
     *
     * @return int，默认为 0
     */
    public static int getAsInt(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0;
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return 0;
        }
        return jsonNode.isInt() ? jsonNode.intValue() : Integer.parseInt(getAsString(jsonNode));
    }

    /**
     * 从json串中获取某个字段
     *
     * @return long，默认为 0
     */
    public static long getAsLong(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0L;
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return 0L;
        }
        return jsonNode.isLong() ? jsonNode.longValue() : Long.parseLong(getAsString(jsonNode));
    }

    /**
     * 从json串中获取某个字段
     *
     * @return double，默认为 0.0
     */
    public static double getAsDouble(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return 0.0;
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return 0.0;
        }
        return jsonNode.isDouble() ? jsonNode.doubleValue() : Double.parseDouble(getAsString(jsonNode));
    }

    /**
     * 从json串中获取某个字段
     *
     * @return BigInteger，默认为 0.0
     */
    public static BigInteger getAsBigInteger(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new BigInteger(String.valueOf(0.00));
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return new BigInteger(String.valueOf(0.00));
        }
        return jsonNode.isBigInteger() ? jsonNode.bigIntegerValue() : new BigInteger(getAsString(jsonNode));
    }

    /**
     * 从json串中获取某个字段
     *
     * @return BigDecimal，默认为 0.00
     */
    public static BigDecimal getAsBigDecimal(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new BigDecimal("0.00");
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return new BigDecimal("0.00");
        }
        return jsonNode.isBigDecimal() ? jsonNode.decimalValue() : new BigDecimal(getAsString(jsonNode));
    }

    /**
     * 从json串中获取某个字段
     *
     * @return boolean, 默认为false
     */
    public static boolean getAsBoolean(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return false;
        }
        if (jsonNode.isBoolean()) {
            return jsonNode.booleanValue();
        } else {
            if (jsonNode.isString()) {
                String textValue = jsonNode.stringValue();
                return Convert.toBool(textValue);
            } else {//number
                return BooleanUtils.toBoolean(jsonNode.intValue());
            }
        }
    }

    /**
     * 从json串中获取某个字段
     *
     * @return byte[], 默认为 null
     */
    public static byte[] getAsBytes(String json, String key) {
        if (StringUtils.isEmpty(json)) {
            return new byte[0];
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return new byte[0];
        }
        return jsonNode.isBinary() ? jsonNode.binaryValue() : getAsString(jsonNode).getBytes();
    }

    /**
     * 从json串中获取某个字段
     *
     * @return object, 默认为 null
     */
    public static <V> V getAsObject(String json, String key, Class<V> type) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructType(type);
        return from(getAsString(jsonNode), javaType);
    }


    /**
     * 从json串中获取某个字段
     *
     * @return list, 默认为 null
     */
    public static <V> List<V> getAsList(String json, String key, Class<V> type) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        JsonNode jsonNode = getAsJsonObject(json, key);
        if (null == jsonNode) {
            return Collections.emptyList();
        }
        CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, type);
        return from(getAsString(jsonNode), collectionType);
    }

    /**
     * 从json串中获取某个字段
     *
     * @return JsonNode, 默认为 null
     */
    public static JsonNode getAsJsonObject(String json, String key) {
        JsonNode node = mapper.readTree(json);
        if (null == node) {
            return null;
        }
        return node.get(key);
    }

    /**
     * 向json中添加属性
     *
     * @return json
     */
    public static <V> String add(String json, String key, V value) {
        JsonNode node = mapper.readTree(json);
        add(node, key, value);
        return node.toString();
    }

    /**
     * 向json中添加属性
     */
    private static <V> void add(JsonNode jsonNode, String key, V value) {
        switch (value) {
            case String s -> ((ObjectNode) jsonNode).put(key, s);
            case Short i -> ((ObjectNode) jsonNode).put(key, i);
            case Integer i -> ((ObjectNode) jsonNode).put(key, i);
            case Long l -> ((ObjectNode) jsonNode).put(key, l);
            case Float v -> ((ObjectNode) jsonNode).put(key, v);
            case Double v -> ((ObjectNode) jsonNode).put(key, v);
            case BigDecimal bigDecimal -> ((ObjectNode) jsonNode).put(key, bigDecimal);
            case BigInteger bigInteger -> ((ObjectNode) jsonNode).put(key, bigInteger);
            case Boolean b -> ((ObjectNode) jsonNode).put(key, b);
            case byte[] bytes -> ((ObjectNode) jsonNode).put(key, bytes);
            case null, default -> ((ObjectNode) jsonNode).put(key, to(value));
        }
    }

    /**
     * 除去json中的某个属性
     *
     * @return json
     */
    public static String remove(String json, String key) {
        JsonNode node = mapper.readTree(json);
        ((ObjectNode) node).remove(key);
        return node.toString();
    }

    /**
     * 修改json中的属性
     */
    public static <V> String update(String json, String key, V value) {
        JsonNode node = mapper.readTree(json);
        ((ObjectNode) node).remove(key);
        add(node, key, value);
        return node.toString();
    }

    /**
     * 格式化Json(美化)
     *
     * @return json
     */
    public static String format(String json) {
        JsonNode node = mapper.readTree(json);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    /**
     * 判断字符串是否是json
     *
     * @return json
     */
    public static boolean isJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
