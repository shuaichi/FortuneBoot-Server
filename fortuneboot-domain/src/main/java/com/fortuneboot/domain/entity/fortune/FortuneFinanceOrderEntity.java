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
 * 单据表(报销单、借出单、接入单)
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/8/16 17:56
 */
@Data
@TableName("fortune_finance_order")
@EqualsAndHashCode(callSuper = true)
public class FortuneFinanceOrderEntity extends BaseEntity<FortuneFinanceOrderEntity> {


    @Schema(description = "主键")
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @Schema(description = "账本id")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "单据类型")
    @TableField("type")
    private Integer type;

    @Schema(description = "出金额")
    @TableField("out_amount")
    private BigDecimal outAmount;

    @Schema(description = "入金额")
    @TableField("in_amount")
    private BigDecimal inAmount;

    @Schema(description = "状态")
    @TableField("status")
    private Integer status;

    @Schema(description = "单据提交时间")
    @TableField("submit_time")
    private LocalDateTime submitTime;

    @Schema(description = "单据关闭时间")
    @TableField("close_time")
    private LocalDateTime closeTime;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

}
