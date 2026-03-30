package com.fortuneboot.repository.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.system.SysLoginTokenMapper;
import com.fortuneboot.domain.entity.system.SysLoginTokenEntity;
import com.fortuneboot.repository.system.SysLoginTokenRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录令牌持久化 服务实现类
 *
 * @author fortuneboot
 */
@Service
public class SysLoginTokenRepoImpl extends ServiceImpl<SysLoginTokenMapper, SysLoginTokenEntity>
        implements SysLoginTokenRepo {

    @Override
    public SysLoginTokenEntity getByTokenKey(String tokenKey) {
        LambdaQueryWrapper<SysLoginTokenEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginTokenEntity::getTokenKey, tokenKey);
        return this.getOne(wrapper);
    }

    @Override
    public boolean removeByTokenKey(String tokenKey) {
        LambdaQueryWrapper<SysLoginTokenEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysLoginTokenEntity::getTokenKey, tokenKey);
        return this.remove(wrapper);
    }

    @Override
    public int cleanExpiredTokens() {
        LambdaQueryWrapper<SysLoginTokenEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(SysLoginTokenEntity::getExpireTime, LocalDateTime.now());
        long count = this.count(wrapper);
        this.remove(wrapper);
        return (int) count;
    }

    @Override
    public List<SysLoginTokenEntity> listValidTokens() {
        LambdaQueryWrapper<SysLoginTokenEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(SysLoginTokenEntity::getExpireTime, LocalDateTime.now());
        return this.list(wrapper);
    }

    @Override
    public List<SysLoginTokenEntity> listByTokenKeyPrefix(String prefix) {
        LambdaQueryWrapper<SysLoginTokenEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysLoginTokenEntity::getTokenKey, prefix)
                .ge(SysLoginTokenEntity::getExpireTime, LocalDateTime.now());
        return this.list(wrapper);
    }
}