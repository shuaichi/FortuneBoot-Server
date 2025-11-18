package com.fortuneboot.system.config.model;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.system.SysConfigEntity;
import com.fortuneboot.factory.system.factory.ConfigModelFactory;
import com.fortuneboot.factory.system.model.ConfigModel;
import com.fortuneboot.repository.system.SysConfigRepo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigModelTest {

    private final SysConfigRepo configService = mock(SysConfigRepo.class);

    private final ConfigModelFactory configModelFactory = new ConfigModelFactory(configService);

    private final Long CONFIG_ID = 1L;

    @Test
    void testBeanUtilsCopyFunction() {
        SysConfigEntity entity = getConfigEntity();
        when(configService.getById(any())).thenReturn(entity);

        ConfigModel configModel = configModelFactory.loadById(CONFIG_ID);

        Assertions.assertEquals(entity.getConfigId(), configModel.getConfigId());
        Assertions.assertEquals(entity.getConfigKey(), configModel.getConfigKey());
        Assertions.assertEquals(entity.getConfigValue(), configModel.getConfigValue());
        Assertions.assertEquals(entity.getConfigName(), configModel.getConfigName());
        Assertions.assertEquals(entity.getConfigOptions(), configModel.getConfigOptions());
        Assertions.assertEquals(entity.getIsAllowChange(), configModel.getIsAllowChange());
        Assertions.assertEquals(entity.getRemark(), configModel.getRemark());
        Assertions.assertEquals(entity.getCreateTime(), configModel.getCreateTime());
        Assertions.assertEquals(entity.getCreatorId(), configModel.getCreatorId());
        Assertions.assertEquals(2, configModel.getConfigOptionSet().size());
    }

    @Test
    void testConfigModelConstructor() {
        SysConfigEntity entity = getConfigEntity();
        when(configService.getById(any())).thenReturn(entity);

        ConfigModel configModel = configModelFactory.loadById(CONFIG_ID);

        Assertions.assertTrue(configModel.getConfigOptionSet().contains("true"));
    }

    @Test
    void testConfigModelConstructorWhenInvalidJson() {
        SysConfigEntity entity = getConfigEntity();
        entity.setConfigOptions("{\"true\",\"false\"}");
        ConfigModel invalid1 = new ConfigModel(entity, configService);
        entity.setConfigOptions("\"[]\"");
        ConfigModel invalid2 = new ConfigModel(entity, configService);
        entity.setConfigOptions("\"xxxx\"");
        ConfigModel invalid3 = new ConfigModel(entity, configService);

        Assertions.assertEquals(0, invalid1.getConfigOptionSet().size());
        Assertions.assertEquals(0, invalid2.getConfigOptionSet().size());
        Assertions.assertEquals(0, invalid3.getConfigOptionSet().size());
    }


    @Test
    void checkCanBeModifyWhenValueEmpty() {
        ConfigModel configModel = configModelFactory.create();

        configModel.setConfigValue(null);
        ApiException exception1 = Assertions.assertThrows(ApiException.class, configModel::checkCanBeModify);
        configModel.setConfigValue("");
        ApiException exception2 = Assertions.assertThrows(ApiException.class, configModel::checkCanBeModify);

        Assertions.assertEquals(ErrorCode.Business.CONFIG_VALUE_IS_NOT_ALLOW_TO_EMPTY, exception1.getErrorCode());
        Assertions.assertEquals(ErrorCode.Business.CONFIG_VALUE_IS_NOT_ALLOW_TO_EMPTY, exception2.getErrorCode());
    }

    private SysConfigEntity getConfigEntity() {
        SysConfigEntity entity = new SysConfigEntity();
        entity.setConfigId(1);
        entity.setConfigKey("testKey");
        entity.setConfigName("testName");
        entity.setConfigValue("testValue");
        entity.setConfigOptions("[\"true\",\"false\"]");
        entity.setIsAllowChange(false);
        entity.setRemark("备注");
        entity.setCreatorId(1L);
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

}
