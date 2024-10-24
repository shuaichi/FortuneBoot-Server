package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交易标签和账单的关系表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:33
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneTagRelationEntity extends BaseEntity<FortuneTagRelationEntity> {

    @Schema(description = "主键")
    @TableId(value = "tag_relation_id",type = IdType.AUTO)
    private Long tagRelationId;

    @Schema(description = "账单流水ID")
    @TableField("account_flow_id")
    private Long accountFlowId;

    @Schema(description = "标签ID")
    @TableField("tag_id")
    private Long tagId;
}
