package com.fortuneboot.infrastructure.config.redis;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * @author valarchie
 */
@Slf4j
@Configuration
@ConditionalOnExpression("'${fortuneboot.embedded.redis}' == 'true'")
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port}")
    private Integer port;

    private RedisServer redisServer;


    @PostConstruct
    public void postConstruct() {
        try {
            RedisServer redisServer = RedisServer.newRedisServer().port(port)
                    .setting("maxmemory 32M")
                    .setting("daemonize no")
                    .setting("appendonly no").build();
            this.redisServer = redisServer;
            redisServer.start();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        try {
            redisServer.stop();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
