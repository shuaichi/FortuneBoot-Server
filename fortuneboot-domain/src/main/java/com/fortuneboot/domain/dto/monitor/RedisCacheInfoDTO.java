package com.fortuneboot.domain.dto.monitor;

import java.util.List;
import java.util.Properties;
import lombok.Data;


/**
 * @author valarchie
 */
@Data
public class RedisCacheInfoDTO {

    private Properties info;
    private Long dbSize;
    private List<CommandStatusDTO> commandStats;

    @Data
    public static class CommandStatusDTO {
        private String name;
        private String value;
    }

}
