package com.fortuneboot.admin.config;


import java.io.File;

import com.fortuneboot.common.config.FortuneBootConfig;
import com.fortuneboot.common.constant.Constants;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@ExtendWith(SpringExtension.class)
@AllArgsConstructor
public class FortuneBootConfigTest {

    private final FortuneBootConfig config;

    @Test
    public void testConfig() {
        String fileBaseDir = "D:\\fortune\\profile";

        Assertions.assertEquals("Fortune", config.getName());
        Assertions.assertEquals("1.0.0", config.getVersion());
        Assertions.assertEquals("2022", config.getCopyrightYear());
        Assertions.assertTrue(config.isDemoEnabled());
        Assertions.assertEquals(fileBaseDir, FortuneBootConfig.getFileBaseDir());
        Assertions.assertFalse(FortuneBootConfig.isAddressEnabled());
        Assertions.assertEquals("math", FortuneBootConfig.getCaptchaType());
        Assertions.assertEquals("math", FortuneBootConfig.getCaptchaType());
        Assertions.assertEquals(fileBaseDir + "\\import",
                FortuneBootConfig.getFileBaseDir() + File.separator + Constants.UploadSubDir.IMPORT_PATH);
        Assertions.assertEquals(fileBaseDir + "\\avatar",
                FortuneBootConfig.getFileBaseDir() + File.separator + Constants.UploadSubDir.AVATAR_PATH);
        Assertions.assertEquals(fileBaseDir + "\\download",
                FortuneBootConfig.getFileBaseDir() + File.separator + Constants.UploadSubDir.DOWNLOAD_PATH);
        Assertions.assertEquals(fileBaseDir + "\\upload",
                FortuneBootConfig.getFileBaseDir() + File.separator + Constants.UploadSubDir.UPLOAD_PATH);
    }

}
