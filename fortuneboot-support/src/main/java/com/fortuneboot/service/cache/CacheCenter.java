package com.fortuneboot.service.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.fortuneboot.domain.registry.CacheRegistry;
import com.fortuneboot.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import com.fortuneboot.infrastructure.cache.redis.RedisCacheTemplate;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * 缓存中心  提供全局访问点
 * 如果是领域类的缓存  可以自己新建一个直接放在CacheCenter   不用放在infrastructure包里的GuavaCacheService
 * 或者RedisCacheService
 *
 * @author valarchie
 */
@Component
public class CacheCenter {

    public static AbstractGuavaCacheTemplate<String> configCache;

    public static RedisCacheTemplate<String> captchaCache;

    public static RedisCacheTemplate<SystemLoginUser> loginUserCache;

    public static RedisCacheTemplate<SysUserEntity> userCache;

    public static RedisCacheTemplate<SysRoleEntity> roleCache;


    @PostConstruct
    public void init() {
        GuavaCacheService guavaCache = SpringUtil.getBean(GuavaCacheService.class);
        RedisCacheService redisCache = SpringUtil.getBean(RedisCacheService.class);
        configCache = guavaCache.configCache;
        captchaCache = redisCache.captchaCache;
        loginUserCache = redisCache.loginUserCache;
        userCache = redisCache.userCache;
        roleCache = redisCache.roleCache;
        CacheRegistry.registerRoleCache((roleId) -> roleCache.getObjectById(roleId));
        CacheRegistry.registerUserCache((userId) -> userCache.getObjectById(userId));
    }

}
