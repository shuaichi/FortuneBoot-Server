package com.fortuneboot.domain.dto;

import com.fortuneboot.common.enums.common.ConfigKeyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

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
    private String defaultValue;
    private Boolean isAllowChange;
    private String remark;

    public configKeyDTO(ConfigKeyEnum configKeyEnum) {
        if (Objects.nonNull(configKeyEnum)) {
            BeanUtils.copyProperties(configKeyEnum, this);
        }
    }

}
