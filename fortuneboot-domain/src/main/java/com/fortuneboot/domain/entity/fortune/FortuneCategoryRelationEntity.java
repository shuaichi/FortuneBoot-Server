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
 * @author zhangchi118
 * @date 2025/1/29 23:25
 **/
@Data
@TableName("fortune_category_relation")
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryRelationEntity extends BaseEntity<FortuneCategoryRelationEntity> {

    @Schema(description = "主键")
    @TableId(value = "category_relation_id", type = IdType.AUTO)
    private Long categoryRelationId;

    @Schema(description = "分类id")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "账单id")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "金额")
    @TableField("amount")
    private BigDecimal amount;
}
