package com.fortuneboot.domain.bo.fortune;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 货币Template
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/16 23:39
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyTemplateBo {

    /**
     * 主键
     */
    private Long currencyId;

    /**
     * 模板名称
     */
    private String currencyName;

    /**
     * 备注
     */
    private String description;

    /**
     * 汇率
     */
    private Double rate;

    /**
     * 构造方法
     *
     * @param currencyId
     * @param currencyName
     * @param rate
     */
    public CurrencyTemplateBo(Long currencyId, String currencyName, Double rate) {
        this.currencyId = currencyId;
        this.currencyName = currencyName;
        this.rate = rate;
    }
}
