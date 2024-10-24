package com.fortuneboot.service.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.infrastructure.cache.RedisUtil;
import com.fortuneboot.infrastructure.cache.redis.CacheKeyEnum;
import com.fortuneboot.infrastructure.cache.redis.RedisCacheTemplate;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import java.io.Serializable;

import com.fortuneboot.repository.system.SysRoleRepository;
import com.fortuneboot.repository.system.SysUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisUtil redisUtil;

    public RedisCacheTemplate<String> captchaCache;
    public RedisCacheTemplate<SystemLoginUser> loginUserCache;
    public RedisCacheTemplate<SysUserEntity> userCache;
    public RedisCacheTemplate<SysRoleEntity> roleCache;

    @PostConstruct
    public void init() {

        captchaCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.CAPTCHAT);

        loginUserCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.LOGIN_USER_KEY);

        userCache = new RedisCacheTemplate<SysUserEntity>(redisUtil, CacheKeyEnum.USER_ENTITY_KEY) {
            @Override
            public SysUserEntity getObjectFromDb(Object id) {
                SysUserRepository userRepository = SpringUtil.getBean(SysUserRepository.class);
                return userRepository.getById((Serializable) id);
            }
        };

        roleCache = new RedisCacheTemplate<SysRoleEntity>(redisUtil, CacheKeyEnum.ROLE_ENTITY_KEY) {
            @Override
            public SysRoleEntity getObjectFromDb(Object id) {
                SysRoleRepository roleRepository = SpringUtil.getBean(SysRoleRepository.class);
                return roleRepository.getById((Serializable) id);
            }
        };
    }
}