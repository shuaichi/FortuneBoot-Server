package com.fortuneboot.domain.vo.fortune;

import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 汇率 Vo 对象
 * @author zhangchi118
 * @date 2025/4/12 21:01
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FortuneCurrencyVo {

    /**
     * 构造方法
     *
     * @param currencyTemplateBo
     * @param baseCurrency
     */
    public FortuneCurrencyVo(CurrencyTemplateBo currencyTemplateBo, String baseCurrency) {
        this.currencyName = currencyTemplateBo.getCurrencyName();
        this.rate = currencyTemplateBo.getRate();
        this.baseCurrency = StringUtils.isEmpty(baseCurrency)? "USD" : baseCurrency;
    }

    /**
     * 汇率名称
     */
    private String currencyName;

    /**
     * 汇率
     */
    private BigDecimal rate;

    /**
     * 基准汇率
     */
    private String baseCurrency;
}
