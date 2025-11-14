package com.fortuneboot.infrastructure.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.module.SimpleModule;

import java.time.Duration;


@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 如果使用 Spring Boot 默认配置，可直接返回 LettuceConnectionFactory()
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置 Jackson3 ObjectMapper
        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModules(new SimpleModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .changeDefaultVisibility(vc ->
                        vc.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withCreatorVisibility(JsonAutoDetect.Visibility.ANY)
                ).activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(), DefaultTyping.NON_FINAL)
                .build();

        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModules(new SimpleModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .changeDefaultVisibility(vc ->
                        vc.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                                .withCreatorVisibility(JsonAutoDetect.Visibility.ANY)
                ).activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(), DefaultTyping.NON_FINAL)
                .build();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJacksonJsonRedisSerializer(objectMapper)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
