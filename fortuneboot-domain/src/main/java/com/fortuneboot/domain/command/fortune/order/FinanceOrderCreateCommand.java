package com.fortuneboot.domain.command.fortune.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 单据新建Command
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderCreateCommand {

    /**
     * 账本id
     */
    @NotNull(message = "账本id不能为空")
    @Positive(message = "账本id只能是正数")
    private Long bookId;

    /**
     * 请求幂等ID（UUID）
     */
    @NotBlank(message = "请求幂等ID不能为空")
    @Size(max = 64, message = "请求幂等ID长度不能超过64个字符")
    private String requestId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50个字符")
    private String title;

    /**
     * 单据类型
     *
     * @see com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum
     */
    @NotNull(message = "单据类型不能为空")
    @Min(value = 1, message = "单据类型只能是1/2/3")
    @Max(value = 3, message = "单据类型只能是1/2/3")
    private Integer type;

    /**
     * 交易对象id
     */
    private Long counterpartyId;

    /**
     * 交易对象名称
     */
    @NotBlank(message = "交易对象名称不能为空")
    @Size(max = 64, message = "交易对象名称长度不能超过64个字符")
    private String counterpartyName;

    /**
     * 到期日
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    /**
     * 备注
     */
    @Size(max = 512, message = "备注长度不能超过512个字符")
    private String remark = "";
}
