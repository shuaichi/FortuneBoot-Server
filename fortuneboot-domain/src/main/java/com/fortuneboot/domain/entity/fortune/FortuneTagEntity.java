package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交易标签
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:23
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneTagEntity extends BaseEntity<FortuneTagEntity> {


    @Schema(description = "主键")
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;

    @Schema(description = "标签名称")
    @TableField("tag_name")
    private String tagName;

    @Schema(description = "账本ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "父级ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "可支出")
    @TableField("can_expense")
    private Boolean canExpense;

    @Schema(description = "可收入")
    @TableField("can_income")
    private Boolean canIncome;

    @Schema(description = "可转账")
    @TableField("can_transfer")
    private Boolean canTransfer;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "顺序")
    @TableField("sequence")
    private Integer sequence;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "回收站")
    @TableField("recycleBin")
    private Boolean recycleBin;
}
