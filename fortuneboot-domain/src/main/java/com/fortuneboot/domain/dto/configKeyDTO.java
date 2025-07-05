package com.fortuneboot.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Author: wch
 * @CreateTime: 2025-07-04
 * @Description: 参数枚举对象
 * @Version: 1.0
 */
@Data
@Schema(name = "configKeyDTO", description = "参数枚举信息")
public class configKeyDTO {
    private String value;
    private String description;
    private String option;
    private Boolean required;

    public configKeyDTO(com.fortuneboot.common.enums.common.ConfigKeyEnum configKeyEnum) {
        this.value = configKeyEnum.getValue();
        this.description = configKeyEnum.getDescription();
        this.option = configKeyEnum.getOption();
        this.required = configKeyEnum.getRequired();
    }

}
