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
 * 单据幂等请求表（只保留成功事务回执）
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
@TableName("fortune_finance_order_request")
public class FortuneFinanceOrderRequestEntity {

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "账本id")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "请求幂等ID")
    @TableField("request_id")
    private String requestId;

    @Schema(description = "操作类型")
    @TableField("operation")
    private String operation;

    @Schema(description = "请求内容哈希")
    @TableField("request_hash")
    private String requestHash;

    @Schema(description = "创建者ID")
    @TableField(value = "creator_id", fill = FieldFill.INSERT)
    private Long creatorId;

    @Schema(description = "成功事务回执")
    @TableField("response_json")
    private String responseJson;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
