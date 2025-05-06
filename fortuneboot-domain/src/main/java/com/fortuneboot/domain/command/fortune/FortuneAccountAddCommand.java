package com.fortuneboot.domain.command.fortune;

import com.fortuneboot.common.enums.fortune.AccountTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新增账户
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 15:32
 **/
@Data
public class FortuneAccountAddCommand {

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 账户名
     */
    @NotBlank(message = "账户名称不能为空")
    @Size(max = 50,message = "账户名称长度不能超过50个字符")
    private String accountName;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 账单日
     */
    private LocalDate billDay;

    /**
     * 还款日
     */
    private LocalDate repayDay;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 可转出
     */
    private Boolean canTransferOut;

    /**
     * 可转入
     */
    private Boolean canTransferIn;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 是否计入净资产
     */
    private Boolean include;

    /**
     * 利率
     */
    private BigDecimal apr;

    /**
     * 期初余额
     */
    private BigDecimal initialBalance;

    /**
     * 账户类型
     * @see AccountTypeEnum
     */
    @NotNull(message = "账户类型不能为空")
    @Positive
    private Integer accountType;

    /**
     * 分组id
     */
    @NotNull(message = "分组不能为空")
    @Positive
    private Long groupId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    @Size(max = 512,message = "备注长度不能超过512个字符")
    private String remark;
}
