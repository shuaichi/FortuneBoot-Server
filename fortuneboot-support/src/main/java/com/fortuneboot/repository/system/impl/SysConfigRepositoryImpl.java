package com.fortuneboot.repository.system.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.domain.entity.system.SysConfigEntity;
import com.fortuneboot.dao.system.SysConfigMapper;
import com.fortuneboot.repository.system.SysConfigRepository;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 参数配置表 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-09
 */
@Service
public class SysConfigRepositoryImpl extends ServiceImpl<SysConfigMapper, SysConfigEntity> implements
        SysConfigRepository {

    @Override
    public String getConfigValueByKey(String key) {
        if (StrUtil.isBlank(key)) {
            return StrUtil.EMPTY;
        }
        QueryWrapper<SysConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", key);
        SysConfigEntity one = this.getOne(queryWrapper);
        if (one == null || one.getConfigValue() == null) {
            return StrUtil.EMPTY;
        }
        return one.getConfigValue();
    }

    @Override
    public boolean checkConfigKeyUnique(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return false;
        }
        LambdaQueryWrapper<SysConfigEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(SysConfigEntity.class);
        queryWrapper.eq(SysConfigEntity::getConfigKey, configKey);
        return this.exists(queryWrapper);

    }


}
