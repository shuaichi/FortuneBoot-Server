package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 分组
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 18:12
 **/
@Data
public class FortuneGroupAddCommand {

    /**
     * 分组名称
     */
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 50, message = "分组名称长度不能超过50个字符")
    private String groupName;

    /**
     * 默认币种
     */
    @NotBlank(message = "默认币种不能为空")
    private String defaultCurrency;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 默认账本id
     */
    @NotNull(message = "账本模板不能为空")
    private Long bookTemplate;

    /**
     * 备注
     */
    @Size(max = 512,message = "备注长度不能超过512个字符")
    private String remark;
}
