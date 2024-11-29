package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账户表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 22:01
 */
@Data
@TableName("fortune_account")
@EqualsAndHashCode(callSuper = true)
public class FortuneAccountEntity extends BaseEntity<FortuneAccountEntity> {

    @Schema(description = "主键")
    @TableId(value = "account_id", type = IdType.AUTO)
    private Long accountId;

    @Schema(description = "卡号")
    @TableField("card_no")
    private String cardNo;

    @Schema(description = "账户名")
    @TableField("account_name")
    private String accountName;

    @Schema(description = "余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "账单日")
    @TableField("bill_day")
    private LocalDate billDay;

    @Schema(description = "还款日")
    @TableField("repay_day")
    private LocalDate repayDay;

    @Schema(description = "可支出")
    @TableField("can_expense")
    private Boolean canExpense;

    @Schema(description = "可收入")
    @TableField("can_income")
    private Boolean canIncome;

    @Schema(description = "可转出")
    @TableField("can_transfer_out")
    private Boolean canTransferOut;

    @Schema(description = "可转入")
    @TableField("can_transfer_in")
    private Boolean canTransferIn;

    @Schema(description = "信用额度")
    @TableField("credit_limit")
    private BigDecimal creditLimit;

    @Schema(description = "币种")
    @TableField("currency_code")
    private String currencyCode;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "是否计入净资产")
    @TableField("include")
    private Boolean include;

    @Schema(description = "利率")
    @TableField("apr")
    private BigDecimal apr;

    @Schema(description = "期初余额")
    @TableField("initial_balance")
    private BigDecimal initialBalance;

    /**
     * com.fortuneboot.common.enums.fortune.AccountTypeEnum
     */
    @Schema(description = "账户类型")
    @TableField("account_type")
    private Integer accountType;

    @Schema(description = "组id")
    @TableField("group_id")
    private Long groupId;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "回收站")
    @TableField("recycle_bin")
    private Boolean recycleBin;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
