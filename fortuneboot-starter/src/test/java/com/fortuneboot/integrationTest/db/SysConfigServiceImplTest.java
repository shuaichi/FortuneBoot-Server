package com.fortuneboot.integrationTest.db;

import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.repository.system.SysConfigRepo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@RunWith(SpringRunner.class)
class  SysConfigServiceImplTest {

    @Resource
    SysConfigRepo configRepository;

    @Test
    void testGetConfigValueByKey() {
        String configValue = configRepository.getConfigValueByKey(ConfigKeyEnum.CAPTCHA.getValue());
        Assertions.assertFalse(Boolean.parseBoolean(configValue));
    }

    @Test
    void testGetICPByKey() {
        String configValue = configRepository.getConfigValueByKey(ConfigKeyEnum.ICP.getValue());
        log.info("IPC = {}", configValue);
        Assertions.assertTrue(StringUtils.isNotBlank(configValue));
    }


}
