package com.fortuneboot.domain.registry;

import com.fortuneboot.domain.entity.system.SysRoleEntity;
import com.fortuneboot.domain.entity.system.SysUserEntity;

import java.util.function.Function;

public class CacheRegistry {
    static Function<Long, SysUserEntity> userCache;

    static Function<Long, SysRoleEntity> roleCache;

    public static void registerUserCache(Function<Long, SysUserEntity> userCache) {
        CacheRegistry.userCache = userCache;
    }

    public static SysUserEntity getUserById(Long userId) {
        return userCache.apply(userId);
    }

    public static void registerRoleCache(Function<Long, SysRoleEntity> roleCache) {
        CacheRegistry.roleCache = roleCache;
    }

    public static SysRoleEntity getRoleById(Long roleId) {
        return roleCache.apply(roleId);
    }
}
