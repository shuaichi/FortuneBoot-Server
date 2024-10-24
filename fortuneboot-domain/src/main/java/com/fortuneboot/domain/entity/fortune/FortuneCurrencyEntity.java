package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fortuneboot.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 货币表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 22:15
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneCurrencyEntity extends BaseEntity<FortuneCurrencyEntity> {

    @Schema(description = "货币id")
    @TableId(value = "currency_id", type = IdType.AUTO)
    private Long currencyId;

    @Schema(description = "货币名称")
    @TableField("currency_name")
    private String currencyName;

    @Schema(description = "是否启用")
    @TableField("enable")
    private Boolean enable;

    @Schema(description = "汇率")
    @TableField("rate")
    private BigDecimal rate;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
