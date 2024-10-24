package com.fortuneboot.infrastructure.config;

import com.fortuneboot.common.config.FortuneBootConfig;
import com.fortuneboot.common.constant.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用配置
 *
 * @author valarchie
 */
@Configuration
@RequiredArgsConstructor
public class ResourcesConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /* 本地文件上传路径 */
        registry.addResourceHandler("/" + Constants.RESOURCE_PREFIX + "/**")
            .addResourceLocations("file:" + FortuneBootConfig.getFileBaseDir() + "/");

    }

}
