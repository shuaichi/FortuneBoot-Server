package com.fortuneboot.service.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.infrastructure.cache.RedisUtil;
import com.fortuneboot.infrastructure.cache.redis.CacheKeyEnum;
import com.fortuneboot.infrastructure.cache.redis.RedisCacheTemplate;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import java.io.Serializable;

import com.fortuneboot.repository.system.SysRoleRepo;
import com.fortuneboot.repository.system.SysUserRepo;
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

        captchaCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.CAPTCHA);

        loginUserCache = new RedisCacheTemplate<>(redisUtil, CacheKeyEnum.LOGIN_USER);

        userCache = new RedisCacheTemplate<SysUserEntity>(redisUtil, CacheKeyEnum.USER_ENTITY) {
            @Override
            public SysUserEntity getObjectFromDb(Object id) {
                SysUserRepo userRepository = SpringUtil.getBean(SysUserRepo.class);
                return userRepository.getById((Serializable) id);
            }
        };

        roleCache = new RedisCacheTemplate<SysRoleEntity>(redisUtil, CacheKeyEnum.ROLE_ENTITY) {
            @Override
            public SysRoleEntity getObjectFromDb(Object id) {
                SysRoleRepo roleRepository = SpringUtil.getBean(SysRoleRepo.class);
                return roleRepository.getById((Serializable) id);
            }
        };
    }
}