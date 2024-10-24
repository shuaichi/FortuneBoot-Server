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
 * 用户/分组关系表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:39
 **/
@Data
@TableName("fortune_user_group_relation")
@EqualsAndHashCode(callSuper = true)
public class FortuneUserGroupRelationEntity extends BaseEntity<FortuneUserGroupRelationEntity> {

    @Schema(description = "主键")
    @TableId(value = "user_group_relation_id", type = IdType.AUTO)
    private Long userGroupRelationId;

    /**
     * com.fortuneboot.common.enums.fortune.RoleTypeEnum
     */
    @Schema(description = "权限")
    @TableField("role_type")
    private Integer roleType;

    @Schema(description = "分组ID")
    @TableField("group_id")
    private Long groupId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;
}
