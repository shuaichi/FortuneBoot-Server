package com.fortuneboot.integrationTest.db;

import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.repository.system.SysConfigRepository;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = IntegrationTestApplication.class)
@RunWith(SpringRunner.class)
class  SysConfigServiceImplTest {

    @Resource
    SysConfigRepository configRepository;

    @Test
    void testGetConfigValueByKey() {
        String configValue = configRepository.getConfigValueByKey(ConfigKeyEnum.CAPTCHA.getValue());
        Assertions.assertFalse(Boolean.parseBoolean(configValue));
    }


}
