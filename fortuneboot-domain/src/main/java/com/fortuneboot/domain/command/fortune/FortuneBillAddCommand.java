package com.fortuneboot.domain.command.fortune;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 23:15
 **/
@Data
public class FortuneBillAddCommand {

    /**
     * 账本id
     */
    private Long bookId;

    /**
     *标题
     */
    private String title;

    /**
     *交易时间
     */
    private LocalDateTime tradeTime;

    /**
     *账户id
     */
    private Long accountId;

    /**
     *分类id
     */
    private Long categoryId;

    /**
     *金额
     */
    private BigDecimal amount;

    /**
     *汇率转换后的金额
     */
    private BigDecimal convertedAmount;

    /**
     *交易对象
     */
    private Long payeeId;

    /**
     * 流水类型
     * com.fortuneboot.common.enums.fortune.BillTypeEnum
     */
    private Integer billType;

    /**
     *转账到的账户
     */
    private Long toAccountId;

    /**
     *是否确认
     */
    private Boolean confirm;

    /**
     *是否统计
     */
    private Boolean include;

    /**
     *备注
     */
    private Boolean remark;

    private List<FortuneTagAddCommand> tagList;

}
