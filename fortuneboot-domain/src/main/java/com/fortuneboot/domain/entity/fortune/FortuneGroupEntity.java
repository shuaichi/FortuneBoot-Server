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
 * 分组
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:05
 **/
@Data
@TableName("fortune_group")
@EqualsAndHashCode(callSuper = true)
public class FortuneGroupEntity extends BaseEntity<FortuneGroupEntity> {

    @Schema(description = "主键")
    @TableId(value = "group_id", type = IdType.AUTO)
    private Long groupId;

    @Schema(description = "分组名称")
    @TableField("group_name")
    private String groupName;

    @Schema(description = "默认币种")
    @TableField("default_currency")
    private String defaultCurrency;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "默认操作账本")
    @TableField("default_book_id")
    private Long defaultBookId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
