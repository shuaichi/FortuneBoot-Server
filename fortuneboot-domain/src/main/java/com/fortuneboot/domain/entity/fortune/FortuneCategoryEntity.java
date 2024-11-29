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
 * 分类表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 23:15
 **/
@Data
@TableName("fortune_category")
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryEntity extends BaseEntity<FortuneCategoryEntity> {

    @Schema(description = "主键")
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    @Schema(description = "分类名称")
    @TableField("category_name")
    private String categoryName;

    /**
     * com.fortuneboot.common.enums.fortune.CategoryTypeEnum
     */
    @Schema(description = "分类类型")
    @TableField("category_type")
    private Integer categoryType;

    @Schema(description = "所属账本ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "父级ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "排序")
    @TableField("sequence")
    private Integer sort;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "回收站")
    @TableField("recycle_bin")
    private Boolean recycleBin;
}
