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
@TableName("fortune_member_relation")
@EqualsAndHashCode(callSuper = true)
public class FortuneMemberRelationEntity extends BaseEntity<FortuneMemberRelationEntity> {

    @Schema(description = "主键")
    @TableId(value = "member_relation_id", type = IdType.AUTO)
    private Long memberRelationId;

    @Schema(description = "账单流水ID")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "成员ID")
    @TableField("member_id")
    private Long memberId;
}