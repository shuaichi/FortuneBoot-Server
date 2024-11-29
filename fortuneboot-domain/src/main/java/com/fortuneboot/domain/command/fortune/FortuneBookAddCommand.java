package com.fortuneboot.domain.command.fortune;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 账本
 *
 * @author zhangchi118
 * @date 2024/11/29 15:53
 **/
@Data
public class FortuneBookAddCommand {

    @NotNull
    @Positive
    private Long groupId;

    /**
     * 账本名称
     */
    @NotBlank(message = "账本名称不能为空")
    @Size(max = 50, message = "账本名称长度不能超过50个字符")
    private String bookName;

    /**
     * 默认币种
     */
    @NotBlank(message = "默认币种不能为空")
    private String defaultCurrency;

    /**
     * 默认支出账户ID
     */
    private Long defaultExpenseAccountId;

    /**
     * 默认收入账户ID
     */
    private Long defaultIncomeAccountId;

    /**
     * 默认转出账户ID
     */
    private Long defaultTransferOutAccountId;

    /**
     * 默认转入账户ID
     */
    private Long defaultTransferInAccountId;

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
