package com.fortuneboot.integrationTest.db;

import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.repository.system.SysConfigRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@ExtendWith(SpringExtension.class)
@AllArgsConstructor
public class SysConfigServiceImplTest {

    private final SysConfigRepo configRepository;

    @Test
    void testGetConfigValueByKey() {
        String configValue = configRepository.getConfigValueByKey(ConfigKeyEnum.CAPTCHA.getValue());
        Assertions.assertFalse(Boolean.parseBoolean(configValue));
    }

    @Test
    void testGetIcpByKey() {
        String configValue = configRepository.getConfigValueByKey(ConfigKeyEnum.ICP.getValue());
        log.info("IPC = {}", configValue);
        Assertions.assertTrue(StringUtils.isNotBlank(configValue));
    }


}
