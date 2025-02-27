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
 * 账单文件表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 22:51
 **/
@Data
@TableName("fortune_file")
@EqualsAndHashCode(callSuper = true)
public class FortuneFileEntity extends BaseEntity<FortuneFileEntity> {

    @Schema(description = "主键")
    @TableId(value = "file_id", type = IdType.AUTO)
    private Long fileId;

    @Schema(description = "内容类型")
    @TableField("content_type")
    private String contentType;

    @Schema(description = "文件数据")
    @TableField("file_data")
    private byte[] fileData;

    @Schema(description = "文件大小")
    @TableField("size")
    private Long size;

    @Schema(description = "原始名称")
    @TableField("original_name")
    private String originalName;

    @Schema(description = "账单ID")
    @TableField("bill_id")
    private Long billId;
}
