package com.fortuneboot.factory.system.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.command.system.ConfigAddCommand;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fortuneboot.domain.command.system.ConfigUpdateCommand;
import com.fortuneboot.domain.entity.system.SysConfigEntity;
import com.fortuneboot.repository.system.SysConfigRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigModel extends SysConfigEntity {

    private SysConfigRepository configRepository;

    private Set<String> configOptionSet;

    public ConfigModel(SysConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public ConfigModel(SysConfigEntity entity, SysConfigRepository configRepository) {
        BeanUtil.copyProperties(entity, this);

        List<String> options =
            JSONUtil.isTypeJSONArray(entity.getConfigOptions()) ? JSONUtil.toList(entity.getConfigOptions(),
                String.class) : ListUtil.empty();

        this.configOptionSet = new HashSet<>(options);

        this.configRepository = configRepository;
    }

    public void loadUpdateCommand(ConfigUpdateCommand updateCommand) {
        this.setConfigValue(updateCommand.getConfigValue());
    }


    public void checkCanBeModify() {
        if (StrUtil.isBlank(getConfigValue())) {
            throw new ApiException(ErrorCode.Business.CONFIG_VALUE_IS_NOT_ALLOW_TO_EMPTY);
        }

        if (!configOptionSet.isEmpty() && !configOptionSet.contains(getConfigValue())) {
            throw new ApiException(ErrorCode.Business.CONFIG_VALUE_IS_NOT_IN_OPTIONS);
        }
    }

    public void loadAddCommand(ConfigAddCommand configAddCommand) {
        this.setConfigId(null);
        this.setConfigName(configAddCommand.getConfigName());
        this.setConfigKey(configAddCommand.getConfigKey());
        this.setConfigValue(configAddCommand.getConfigValue());
        this.setConfigOptions(configAddCommand.getConfigOptions());
        this.setIsAllowChange(configAddCommand.getIsAllowChange());
        this.setRemark(configAddCommand.getRemark());
    }

    public void checkConfigKeyUnique() {
        if (configRepository.checkConfigKeyUnique(getConfigKey())) {
            throw new ApiException(ErrorCode.Business.CONFIG_KEY_IS_NOT_UNIQUE, getConfigKey());
        }
    }

}
