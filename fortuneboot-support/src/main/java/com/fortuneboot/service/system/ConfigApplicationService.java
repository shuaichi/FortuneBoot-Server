package com.fortuneboot.service.system;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import com.fortuneboot.domain.command.system.ConfigAddCommand;
import com.fortuneboot.domain.dto.configKeyDTO;
import com.fortuneboot.domain.dto.monitor.CpuInfo;
import com.fortuneboot.factory.system.model.MenuModel;
import com.fortuneboot.service.cache.CacheCenter;
import com.fortuneboot.domain.command.system.ConfigUpdateCommand;
import com.fortuneboot.domain.dto.ConfigDTO;
import com.fortuneboot.factory.system.model.ConfigModel;
import com.fortuneboot.domain.query.system.ConfigQuery;
import com.fortuneboot.domain.entity.system.SysConfigEntity;
import com.fortuneboot.factory.system.ConfigModelFactory;
import com.fortuneboot.repository.system.SysConfigRepository;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author valarchie
 */
@Service
@RequiredArgsConstructor
public class ConfigApplicationService {

    private final ConfigModelFactory configModelFactory;

    private final SysConfigRepository configRepository;

    public PageDTO<ConfigDTO> getConfigList(ConfigQuery query) {
        Page<SysConfigEntity> page = configRepository.page(query.toPage(), query.toQueryWrapper());
        List<ConfigDTO> records = page.getRecords().stream().map(ConfigDTO::new).collect(Collectors.toList());
        return new PageDTO<>(records, page.getTotal());
    }

    public ConfigDTO getConfigInfo(Long id) {
        SysConfigEntity byId = configRepository.getById(id);
        return new ConfigDTO(byId);
    }

    public void updateConfig(ConfigUpdateCommand updateCommand) {
        ConfigModel configModel = configModelFactory.loadById(updateCommand.getConfigId());
        configModel.loadUpdateCommand(updateCommand);

        configModel.checkCanBeModify();

        configModel.updateById();

        CacheCenter.configCache.invalidate(configModel.getConfigKey());
    }

    public PageDTO<configKeyDTO> getSystemConfigOptions() {
        List<ConfigKeyEnum> configKeyDTOS = ConfigKeyEnum.getAllConfigKeys();
        List<configKeyDTO> records = configKeyDTOS.stream().map(configKeyDTO::new).toList();
        Page<configKeyDTO> page = new Page<>(1, records.size());
        return new PageDTO<>(records, page.getTotal());

    }

    public void addSystemConfig(ConfigAddCommand configAddCommand) {
        ConfigModel configModel = configModelFactory.create();
        configModel.loadAddCommand(configAddCommand);
        configModel.checkConfigKeyUnique();
        configModel.insert();
    }

    public void deleteSystemConfig(Long configId) {
        ConfigModel configModel = configModelFactory.loadById(configId);
        configModel.deleteById();
    }
}
