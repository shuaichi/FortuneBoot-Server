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
 *
 * @author zhangchi118
 * @date 2026/3/19 09:24
 **/
@Data
@TableName("fortune_member")
@EqualsAndHashCode(callSuper = true)
public class FortuneMemberEntity extends BaseEntity<FortuneMemberEntity> {

    @Schema(description = "主键")
    @TableId(value = "member_id", type = IdType.AUTO)
    private Long memberId;

    @Schema(description = "账本ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "成员名称")
    @TableField("member_name")
    private String memberName;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "回收站")
    @TableField("recycle_bin")
    private Boolean recycleBin;
}