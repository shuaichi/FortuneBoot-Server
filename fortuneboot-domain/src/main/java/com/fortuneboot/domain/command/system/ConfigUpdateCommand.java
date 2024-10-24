package com.fortuneboot.domain.command.system;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * @author valarchie
 */
@Data
@Schema
public class ConfigUpdateCommand {

    @NotNull
    @Positive
    private Long configId;

    @NotNull
    @NotEmpty
    private String configValue;

}
