package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交易对象表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:13
 **/
@Data
@TableName("fortune_payee")
@EqualsAndHashCode(callSuper = true)
public class FortunePayeeEntity extends BaseEntity<FortunePayeeEntity> {

    @Schema(description = "主键")
    @TableId(value = "payee_id", type = IdType.AUTO)
    private Long payeeId;

    @Schema(description = "交易对象名称")
    @TableField("payee_name")
    private String payeeName;

    @Schema(description = "顺序")
    @TableField("sequence")
    private Integer sequence;

    @Schema(description = "可支出")
    @TableField("can_expense")
    private Boolean canExpense;

    @Schema(description = "可收入")
    @TableField("can_income")
    private Boolean canIncome;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "remark")
    @TableField("备注")
    private String remark;
}
