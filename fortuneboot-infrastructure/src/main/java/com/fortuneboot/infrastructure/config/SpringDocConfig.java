package com.fortuneboot.infrastructure.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author valarchie
 * SpringDoc API文档相关配置
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI fortuneBootApi() {
        return new OpenAPI()
                .info(new Info().title("好记")
                        .description("好记记账管理系统演示")
                        .version("v1.0.7")
                        .license(new License()
                                .name("MIT 3.0")
                                .url("http://fortuneboot.com/")
                        )
                )
//            .externalDocs(new ExternalDocumentation()
//                .description("FortuneBoot后台管理系统接口文档")
                //.url("https://juejin.cn/column/7159946528827080734"));
//                .url(""))
                ;
    }

}
