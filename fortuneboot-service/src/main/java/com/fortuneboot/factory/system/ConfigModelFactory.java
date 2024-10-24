package com.fortuneboot.factory.system;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.system.SysConfigEntity;
import com.fortuneboot.repository.system.SysConfigRepository;
import com.fortuneboot.factory.system.model.ConfigModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配置模型工厂
 * @author valarchie
 */
@Component
@RequiredArgsConstructor
public class ConfigModelFactory {

    private final SysConfigRepository configRepository;

    public ConfigModel loadById(Long configId) {
        SysConfigEntity byId = configRepository.getById(configId);
        if (byId == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, configId, "参数配置");
        }
        return new ConfigModel(byId, configRepository);
    }

    public ConfigModel create() {
        return new ConfigModel(configRepository);
    }


}
