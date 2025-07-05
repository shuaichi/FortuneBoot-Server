package com.fortuneboot.common.enums.common;

import com.fortuneboot.common.enums.BasicEnum;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * 系统配置
 * @author valarchie
 * 对应 sys_config表的config_key字段
 */
@Getter
public enum ConfigKeyEnum implements BasicEnum<String> {

    SKIN_THEME("sys.index.skinName",  "系统皮肤主题", "[\"skin-blue\",\"skin-green\",\"skin-purple\",\"skin-red\",\"skin-yellow\"]", Boolean.TRUE),
    INIT_PASSWORD( "sys.user.initPassword",  "初始密码", null, Boolean.TRUE),
    SIDE_BAR_THEME( "sys.index.sideTheme",  "侧边栏开关", "[\"theme-dark\",\"theme-light\"]", Boolean.TRUE),
    CAPTCHA( "sys.account.captchaOnoff",  "验证码开关", "[\"true\",\"false\"]", Boolean.TRUE),
    REGISTER( "sys.account.registerUser",  "注册开放功能", "[\"true\",\"false\"]", Boolean.TRUE),
    ICP( "sys.config.icp",  "icp备案信息", null, Boolean.TRUE),
    DISPLAY( "sys.config.display",  "金额显示格式化", "[\"true\",\"false\"]", Boolean.TRUE);

    // getters
    private final String value;
    private final String description;
    private final String option;
    private final Boolean required;

    ConfigKeyEnum(String value, String description, String option, Boolean required) {
        this.value = value;
        this.description = description;
        this.option = option;
        this.required = required;
    }
    @Override
    public String getValue() {
        return value;
    }
    @Override
    public String getDescription() {
        return description;
    }

    // 新增方法，将所有枚举项返回给前端
    public static List<ConfigKeyEnum> getAllConfigKeys() {
        return Arrays.stream(values()).collect(Collectors.toList());
    }

}
