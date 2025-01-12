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
import java.time.LocalDateTime;

/**
 * 账单流水表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:08
 **/
@Data
@TableName("fortune_account_flow")
@EqualsAndHashCode(callSuper = true)
public class FortuneBalanceFlowEntity extends BaseEntity<FortuneBalanceFlowEntity> {

    @Schema(description = "主键")
    @TableId(value = "balance_flow_id", type = IdType.AUTO)
    private Long balanceFlowId;

    @Schema(description = "账本id")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "交易时间")
    @TableField("trade_time")
    private LocalDateTime tradeTime;

    @Schema(description = "账户id")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "分类id")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "汇率转换后的金额")
    @TableField("converted_amount")
    private BigDecimal convertedAmount;

    @Schema(description = "交易对象")
    @TableField("payee_id")
    private Long payeeId;

    /**
     * com.fortuneboot.common.enums.fortune.BalanceFlowTypeEnum
     */
    @Schema(description = "流水类型")
    @TableField("account_flow_type")
    private Integer balanceFlowType;

    @Schema(description = "转账到的账户")
    @TableField("to_account_id")
    private Long toAccountId;

    @Schema(description = "是否确认")
    @TableField("confirm")
    private Boolean confirm;

    @Schema(description = "是否统计")
    @TableField("include")
    private Boolean include;

    @Schema(description = "备注")
    @TableField("remark")
    private Boolean remark;

    @Schema(description = "回收站")
    @TableField("recycle_bin")
    private Boolean recycleBin;
}
