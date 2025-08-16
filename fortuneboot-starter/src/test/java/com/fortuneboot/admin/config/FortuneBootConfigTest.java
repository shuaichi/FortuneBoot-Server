package com.fortuneboot.admin.config;


import com.fortuneboot.admin.ApplicationStarter;
import java.io.File;

import com.fortuneboot.common.config.FortuneBootConfig;
import com.fortuneboot.common.constant.Constants;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ApplicationStarter.class)
@RunWith(SpringRunner.class)
public class FortuneBootConfigTest {

    @Resource
    private FortuneBootConfig config;

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
