package com.fortuneboot.infrastructure.cache.redis;

import java.util.concurrent.TimeUnit;

/**
 * @author valarchie
 */
public enum CacheKeyEnum {

    /**
     * Redis各类缓存集合
     */
    CAPTCHA("captcha_codes:", 5, TimeUnit.MINUTES),
    LOGIN_USER("login_tokens:", 30, TimeUnit.DAYS),
    RATE_LIMIT("rate_limit:", 60, TimeUnit.SECONDS),
    USER_ENTITY("user_entity:", 60, TimeUnit.MINUTES),
    ROLE_ENTITY("role_entity:", 60, TimeUnit.MINUTES),
    ROLE_MODEL_INFO("role_model_info:", 60, TimeUnit.MINUTES),
    ;


    CacheKeyEnum(String key, int expiration, TimeUnit timeUnit) {
        this.key = key;
        this.expiration = expiration;
        this.timeUnit = timeUnit;
    }

    private final String key;
    private final int expiration;
    private final TimeUnit timeUnit;

    public String key() {
        return key;
    }

    public int expiration() {
        return expiration;
    }

    public TimeUnit timeUnit() {
        return timeUnit;
    }

}
