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

    /**
     * 根据 userId 查询所有 tokenKey 列表
     */
    List<String> listTokenKeysByUserId(Long userId);

    /**
     * 根据 userId 批量查询 tokenKey 列表
     */
    List<String> listTokenKeysByUserIds(List<Long> userIds);

    /**
     * 根据 userId 删除所有登录令牌
     */
    boolean removeByUserId(Long userId);

    /**
     * 根据 userIds 批量删除登录令牌
     */
    boolean removeByUserIds(List<Long> userIds);
}