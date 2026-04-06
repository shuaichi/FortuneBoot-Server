package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:25
 **/
@Data
public class FortuneMemberAddCommand {
    @NotNull
    @Positive
    private Long bookId;

    @NotBlank(message = "成员名称不能为空")
    @Size(max = 50, message = "成员名称长度不能超过50个字符")
    private String memberName;

    private Boolean enable;
    private Integer sort;

    @Size(max = 512, message = "备注长度不能超过512个字符")
    private String remark;
}