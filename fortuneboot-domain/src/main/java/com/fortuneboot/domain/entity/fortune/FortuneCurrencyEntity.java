package com.fortuneboot.domain.entity.fortune;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fortuneboot.common.core.base.BaseEntity;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 货币表
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 22:15
 **/
@Data
@TableName("fortune_currency")
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class FortuneCurrencyEntity extends BaseEntity<FortuneCurrencyEntity> {

    public FortuneCurrencyEntity(CurrencyTemplateBo bo) {
        this.currencyId = bo.getCurrencyId();
        this.currencyName = bo.getCurrencyName();
        this.rate = bo.getRate();
        this.remark = bo.getRemark();
    }

    @Schema(description = "货币id")
    @TableId(value = "currency_id", type = IdType.AUTO)
    private Long currencyId;

    @Schema(description = "货币名称")
    @TableField("currency_name")
    private String currencyName;

    @Schema(description = "汇率")
    @TableField("rate")
    private BigDecimal rate;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}
