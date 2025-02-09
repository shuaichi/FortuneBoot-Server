package com.fortuneboot.domain.bo.fortune.tenplate;

import lombok.Data;

/**
 * 交易对象模板Bo
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 00:22
 **/
@Data
public class PayeeTemplateBo {

    /**
     * 主键
     */
    private Integer payeeId;

    /**
     * 支付对象名称
     */
    private String payeeName;

    /**
     * 能否支出
     */
    private Boolean canExpense;

    /**
     * 能否收入
     */
    private Boolean canIncome;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
