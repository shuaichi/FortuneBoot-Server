package com.fortuneboot.common.enums.common;

import com.fortuneboot.common.enums.BasicEnum;

import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 系统配置
 *
 * @author valarchie
 * 对应 sys_config表的config_key字段
 */
@Getter
@AllArgsConstructor
public enum ConfigKeyEnum implements BasicEnum<String> {

    SKIN_THEME("sys.index.skinName", "主框架页-默认皮肤样式名称", "[\"skin-blue\",\"skin-green\",\"skin-purple\",\"skin-red\",\"skin-yellow\"]", "skin-blue", Boolean.TRUE, "蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow"),
    INIT_PASSWORD("sys.user.initPassword", "用户管理-账号初始密码", null, "12345678", Boolean.TRUE, "初始化密码 123456"),
    SIDE_BAR_THEME("sys.index.sideTheme", "主框架页-侧边栏主题", "[\"theme-dark\",\"theme-light\"]", "theme-dark", Boolean.TRUE, "深色主题theme-dark，浅色主题theme-light"),
    CAPTCHA("sys.account.captchaOnoff", "账号自助-验证码开关", "[\"true\",\"false\"]", "false", Boolean.TRUE, "是否开启验证码功能（true开启，false关闭）"),
    REGISTER("sys.account.registerUser", "账号自助-是否开启用户注册功能", "[\"true\",\"false\"]", "true", Boolean.TRUE, "是否开启注册用户功能（true开启，false关闭）"),
    ICP("sys.config.icp", "系统配置-ICP备案", null, "暂未配置ICP备案号", Boolean.TRUE, "ICP备案号"),
    DISPLAY("sys.config.display", "首页大屏-金额显示/隐藏设置", "[\"true\",\"false\"]", "true", Boolean.TRUE, "金额显示格式化");

    private final String value;
    private final String description;
    private final String option;
    private final String defaultValue;
    private final Boolean isAllowChange;
    private final String remark;


    // 新增方法，将所有枚举项返回给前端
    public static List<ConfigKeyEnum> getAllConfigKeys() {
        return Arrays.stream(values()).toList();
    }

}
