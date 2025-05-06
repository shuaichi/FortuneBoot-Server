package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author zhangchi118
 * @date 2025/1/10 17:44
 **/
@Data
public class FortunePayeeAddCommand {


    /**
     * 账本id
     */
    @NotNull
    @Positive
    private Long bookId;

    /**
     * 名称
     */
    @NotBlank
    private String payeeName;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 可指出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 备注
     */
    @Size(max = 512,message = "备注长度不能超过512个字符")
    private String remark;
}
