package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author zhangchi118
 * @date 2025/1/10 14:56
 **/
@Data
public class FortuneTagAddCommand {

    /**
     * 账本id
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String tagName;

    /**
     * 图标（Iconify 图标名，如 ep:collection-tag）
     */
    @Size(max = 64, message = "图标名称长度不能超过64个字符")
    private String icon;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 可转账
     */
    private Boolean canTransfer;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 512,message = "备注长度不能超过512个字符")
    private String remark;
}
