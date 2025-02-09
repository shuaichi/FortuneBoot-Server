package com.fortuneboot.domain.bo.fortune.tenplate;

import lombok.Data;

/**
 * 标签模板Bo
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 00:21
 **/
@Data
public class TagTemplateBo {

    /**
     * 主键
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 能否支出
     */
    private Boolean canExpense;

    /**
     * 能否收入
     */
    private Boolean canIncome;

    /**
     * 能否转账
     */
    private Boolean canTransfer;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
