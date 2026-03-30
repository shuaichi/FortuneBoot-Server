package com.fortuneboot.service.cache;

import cn.hutool.extra.spring.SpringUtil;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.entity.system.SysLoginTokenEntity;
import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;
import com.fortuneboot.infrastructure.cache.mem.InMemoryCacheTemplate;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.repository.system.SysLoginTokenRepo;
import com.fortuneboot.repository.system.SysRoleRepo;
import com.fortuneboot.repository.system.SysUserRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务（替代原 RedisCacheService）
 * 使用 Guava 内存缓存 + 数据库持久化（用于登录 Token）
 *
 * @author fortuneboot
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final SysLoginTokenRepo loginTokenRepo;

    public InMemoryCacheTemplate<String> captchaCache;
    public InMemoryCacheTemplate<SystemLoginUser> loginUserCache;
    public InMemoryCacheTemplate<SysUserEntity> userCache;
    public InMemoryCacheTemplate<SysRoleEntity> roleCache;

    @PostConstruct
    public void init() {
        // 验证码缓存：纯内存，5 分钟过期
        captchaCache = new InMemoryCacheTemplate<>("captcha_codes:", 5, TimeUnit.MINUTES);

        // 登录用户缓存：内存 + 数据库持久化，内存缓存 60 分钟，数据库保存 30 天
        loginUserCache = new InMemoryCacheTemplate<SystemLoginUser>("login_tokens:", 60, TimeUnit.MINUTES) {
            @Override
            public SystemLoginUser getObjectFromDb(Object id) {
                try {
                    SysLoginTokenRepo tokenRepo = SpringUtil.getBean(SysLoginTokenRepo.class);
                    SysLoginTokenEntity tokenEntity = tokenRepo.getByTokenKey(id.toString());
                    if (tokenEntity == null) {
                        return null;
                    }
                    // 检查是否过期
                    if (tokenEntity.getExpireTime().isBefore(LocalDateTime.now())) {
                        tokenRepo.removeByTokenKey(id.toString());
                        return null;
                    }
                    return JacksonUtil.from(tokenEntity.getLoginUserJson(), SystemLoginUser.class);
                } catch (Exception e) {
                    log.error("从数据库加载登录用户信息失败, tokenKey: {}", id, e);
                    return null;
                }
            }
        };

        // 用户实体缓存：内存 + DB 回源，60 分钟过期
        userCache = new InMemoryCacheTemplate<SysUserEntity>("user_entity:", 60, TimeUnit.MINUTES) {
            @Override
            public SysUserEntity getObjectFromDb(Object id) {
                SysUserRepo userRepository = SpringUtil.getBean(SysUserRepo.class);
                return userRepository.getById((Serializable) id);
            }
        };

        // 角色实体缓存：内存 + DB 回源，60 分钟过期
        roleCache = new InMemoryCacheTemplate<SysRoleEntity>("role_entity:", 60, TimeUnit.MINUTES) {
            @Override
            public SysRoleEntity getObjectFromDb(Object id) {
                SysRoleRepo roleRepository = SpringUtil.getBean(SysRoleRepo.class);
                return roleRepository.getById((Serializable) id);
            }
        };
    }

    /**
     * 保存登录用户到数据库和缓存
     */
    public void saveLoginUser(String tokenKey, SystemLoginUser loginUser) {
        // 1. 写入内存缓存
        loginUserCache.set(tokenKey, loginUser);

        // 2. 持久化到数据库
        try {
            // 先尝试删除旧记录
            loginTokenRepo.removeByTokenKey(tokenKey);

            SysLoginTokenEntity entity = new SysLoginTokenEntity();
            entity.setTokenKey(tokenKey);
            entity.setLoginUserJson(JacksonUtil.to(loginUser));
            entity.setUsername(loginUser.getUsername());
            entity.setUserId(loginUser.getUserId());
            entity.setLoginIp(loginUser.getLoginInfo() != null ? loginUser.getLoginInfo().getIpAddress() : null);
            entity.setExpireTime(LocalDateTime.now().plusDays(30));
            entity.setCreateTime(LocalDateTime.now());
            loginTokenRepo.save(entity);
        } catch (Exception e) {
            log.error("持久化登录令牌失败, tokenKey: {}", tokenKey, e);
        }
    }

    /**
     * 删除登录用户缓存和数据库记录
     */
    public void removeLoginUser(String tokenKey) {
        loginUserCache.delete(tokenKey);
        try {
            loginTokenRepo.removeByTokenKey(tokenKey);
        } catch (Exception e) {
            log.error("删除登录令牌失败, tokenKey: {}", tokenKey, e);
        }
    }
}