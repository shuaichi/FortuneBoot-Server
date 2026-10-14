package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单据操作审计表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
@TableName("fortune_finance_order_audit")
public class FortuneFinanceOrderAuditEntity {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "账本id")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "单据id")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "流水id")
    @TableField("bill_id")
    private Long billId;

    @Schema(description = "请求幂等ID")
    @TableField("request_id")
    private String requestId;

    @Schema(description = "操作动作")
    @TableField("action")
    private String action;

    @Schema(description = "操作前快照")
    @TableField("before_json")
    private String beforeJson;

    @Schema(description = "操作后快照")
    @TableField("after_json")
    private String afterJson;

    @Schema(description = "操作者ID")
    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
