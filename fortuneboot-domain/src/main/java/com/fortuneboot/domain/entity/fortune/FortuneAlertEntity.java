package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 提醒事项
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/4 22:33
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneAlertEntity extends BaseEntity<FortuneAlertEntity> {

    @Schema(description = "主键")
    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long alertId;

    @Schema(description = "标题")
    @TableField("alert_title")
    private String alertTitle;

    @Schema(description = "开始提醒日期")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "结束提醒日期")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "间隔类型")
    @TableField("interval_type")
    private Integer intervalType;

    @Schema(description = "corn表达式")
    @TableField("corn_expression")
    private String cornExpression;

    @Schema(description = "次数")
    @TableField("times")
    private Integer times;

    @Schema(description = "已执行次数")
    @TableField("run_times")
    private Integer runTimes;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "提醒内容")
    @TableField("content")
    private String content;
}
