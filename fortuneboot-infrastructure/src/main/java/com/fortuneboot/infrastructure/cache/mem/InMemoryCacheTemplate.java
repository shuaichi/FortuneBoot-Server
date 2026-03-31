package com.fortuneboot.infrastructure.cache.mem;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * 纯内存缓存模板（基于 Guava Cache）
 * <p>
 * 适用于验证码、用户/角色实体等不需要跨进程共享的缓存场景。
 * 对于需要持久化的场景（如登录Token），请使用子类覆盖 getObjectFromDb 方法实现 DB 回源。
 *
 * @author fortuneboot
 */
@Slf4j
public class InMemoryCacheTemplate<T> {

    private final LoadingCache<String, Optional<T>> guavaCache;
    private final String keyPrefix;

    public InMemoryCacheTemplate(String keyPrefix, int expiration, TimeUnit timeUnit) {
        this(keyPrefix, expiration, timeUnit, 1024);
    }

    public InMemoryCacheTemplate(String keyPrefix, int expiration, TimeUnit timeUnit, int maximumSize) {
        this.keyPrefix = keyPrefix;
        this.guavaCache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .softValues()
                .expireAfterWrite(expiration, timeUnit)
                .concurrencyLevel(64)
                .initialCapacity(128)
                .build(new CacheLoader<>() {
                    @Override
                    public Optional<T> load(String cachedKey) {
                        T cacheObject = getObjectFromDb(extractId(cachedKey));
                        log.debug("load cache from db, key: {} , value: {}", cachedKey, cacheObject);
                        return Optional.ofNullable(cacheObject);
                    }
                });
    }

    /**
     * 从缓存中获取对象，如果获取不到则从 DB 层面获取
     */
    public T getObjectById(Object id) {
        String cachedKey = generateKey(id);
        try {
            Optional<T> optional = guavaCache.get(cachedKey);
            if (optional.isEmpty()) {
                T objectFromDb = getObjectFromDb(id);
                set(id, objectFromDb);
                return objectFromDb;
            }
            return optional.get();
        } catch (ExecutionException e) {
            log.error("从缓存中获取对象失败", e);
            return null;
        }
    }

    /**
     * 仅从缓存获取对象，不回源 DB
     */
    public T getObjectOnlyInCacheById(Object id) {
        String cachedKey = generateKey(id);
        return getObjectOnlyInCacheByKey(cachedKey);
    }

    /**
     * 通过完整 key 仅从缓存获取对象
     */
    public T getObjectOnlyInCacheByKey(String cachedKey) {
        try {
            Optional<T> optional = guavaCache.get(cachedKey);
            return optional.orElse(null);
        } catch (ExecutionException e) {
            log.error("从缓存中获取对象失败", e);
            return null;
        }
    }

    public void set(Object id, T obj) {
        if (obj != null) {
            guavaCache.put(generateKey(id), Optional.of(obj));
        }
    }

    public void delete(Object id) {
        guavaCache.invalidate(generateKey(id));
    }

    public void refresh(Object id) {
        guavaCache.refresh(generateKey(id));
    }

    public String generateKey(Object id) {
        return keyPrefix + id;
    }

    /**
     * 从 key 中提取 id 部分
     */
    private Object extractId(String cachedKey) {
        if (cachedKey != null && cachedKey.startsWith(keyPrefix)) {
            return cachedKey.substring(keyPrefix.length());
        }
        return cachedKey;
    }

    /**
     * 从数据库加载数据，子类可覆盖实现 DB 回源
     */
    public T getObjectFromDb(Object id) {
        return null;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }
}