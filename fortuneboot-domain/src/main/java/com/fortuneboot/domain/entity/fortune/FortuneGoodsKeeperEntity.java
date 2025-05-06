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
 * 归物实体类
 *
 * @author zhangchi118
 * @date 2025/5/6 10:34
 **/
@Data
@TableName("fortune_goods_keeper")
@EqualsAndHashCode(callSuper = true)
public class FortuneGoodsKeeperEntity extends BaseEntity<FortuneGoodsKeeperEntity> {

    @Schema(description = "主键")
    @TableId(value = "goods_keeper_id", type = IdType.AUTO)
    private Long goodsKeeperId;

    @Schema(description = "所属账本id")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "物品名称")
    @TableField("goods_name")
    private String goodsName;

    @Schema(description = "分类id")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "标签id")
    @TableField("tag_id")
    private Long tagId;

    @Schema(description = "价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "购买日期")
    @TableField("purchase_date")
    private LocalDate purchaseDate;

    @Schema(description = "保修日期")
    @TableField("warranty_date")
    private LocalDate warrantyDate;

    @Schema(description = "按次使用")
    @TableField("use_by_times")
    private Boolean useByTimes;

    @Schema(description = "使用次数")
    @TableField("usage_num")
    private Long usageNum;

    @Schema(description = "状态")
    @TableField("status")
    private Integer status;

    @Schema(description = "售出价格")
    @TableField("sold_price")
    private Integer soldPrice;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

}
