package com.fortuneboot.domain.command.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @Author: wch
 * @CreateTime: 2025-07-04
 * @Description: 参数新增对象
 * @Version: 1.0
 */
@Data
@Schema(name = "ConfigAddCommand", description = "参数新增对象")
public class ConfigAddCommand {

    private String configName;

    private String configKey;

    private String configValue;

    private String configOptions;

    private Boolean isAllowChange;

    private  String remark;

}
