package com.fortuneboot.repository.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.system.SysLoginTokenEntity;

import java.util.List;

/**
 * 登录令牌持久化 服务接口
 *
 * @author fortuneboot
 */
public interface SysLoginTokenRepo extends IService<SysLoginTokenEntity> {

    /**
     * 根据 tokenKey 查询登录令牌
     */
    SysLoginTokenEntity getByTokenKey(String tokenKey);

    /**
     * 根据 tokenKey 删除登录令牌
     */
    boolean removeByTokenKey(String tokenKey);

    /**
     * 清理过期的登录令牌
     */
    int cleanExpiredTokens();

    /**
     * 获取所有未过期的登录令牌
     */
    List<SysLoginTokenEntity> listValidTokens();

    /**
     * 根据 tokenKey 模糊查询匹配的令牌
     */
    List<SysLoginTokenEntity> listByTokenKeyPrefix(String prefix);
}