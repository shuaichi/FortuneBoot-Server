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

/**
 * 账单附加费用（手续费/优惠）
 *
 * @author zhangchi118
 **/
@Data
@TableName("fortune_bill_extra")
@EqualsAndHashCode(callSuper = true)
public class FortuneBillExtraEntity extends BaseEntity<FortuneBillExtraEntity> {

    @Schema(description = "主键")
    @TableId(value = "extra_id", type = IdType.AUTO)
    private Long extraId;

    @Schema(description = "关联账单id")
    @TableField("bill_id")
    private Long billId;

    /**
     * 1-手续费，2-优惠
     */
    @Schema(description = "附加类型：1-手续费，2-优惠")
    @TableField("extra_type")
    private Integer extraType;

    @Schema(description = "金额")
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 1-转出账户(from)，2-转入账户(to)
     */
    @Schema(description = "账户方向：1-转出账户(from)，2-转入账户(to)")
    @TableField("account_side")
    private Integer accountSide;

    @Schema(description = "关联分类id（可为空）")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}