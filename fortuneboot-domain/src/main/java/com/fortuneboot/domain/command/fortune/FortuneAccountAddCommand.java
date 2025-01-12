package com.fortuneboot.domain.command.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * com.fortuneboot.common.enums.fortune.AccountTypeEnum
     */
    private Integer accountType;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
