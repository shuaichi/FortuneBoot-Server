package com.fortuneboot.repository.system;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.system.SysConfigEntity;

/**
 * <p>
 * 参数配置表 服务类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-09
 */
public interface SysConfigRepository extends IService<SysConfigEntity> {

    /**
     * 通过key获取配置
     *
     * @param key 配置对应的key
     * @return 配置
     */
    String getConfigValueByKey(String key);

    /**
     * 校验参数键是否唯一
     */
    boolean checkConfigKeyUnique(String configKey );

}
