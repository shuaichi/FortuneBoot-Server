package com.fortuneboot.repository.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.domain.entity.system.SysOperationLogEntity;
import com.fortuneboot.dao.system.SysOperationLogMapper;
import com.fortuneboot.repository.system.SysOperationLogRepository;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * @author valarchie
 * @since 2022-06-08
 */
@Service
public class SysOperationLogRepositoryImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLogEntity> implements
        SysOperationLogRepository {

}
