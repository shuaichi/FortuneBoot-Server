package com.fortuneboot.repository.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.domain.entity.system.SysLoginInfoEntity;
import com.fortuneboot.dao.system.SysLoginInfoMapper;
import com.fortuneboot.repository.system.SysLoginInfoRepo;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统访问记录 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-07-10
 */
@Service
public class SysLoginInfoRepoImpl extends ServiceImpl<SysLoginInfoMapper, SysLoginInfoEntity> implements
        SysLoginInfoRepo {

}
