package com.fortuneboot.domain.bo.fortune.tenplate;

import lombok.Data;

import java.util.List;

/**
 * 账本模板Vo
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/16 23:38
 **/
@Data
public class BookTemplateBo {
    /**
     * 主键
     */
    private Long bookTemplateId;

    /**
     * 模板名称
     */
    private String bookTemplateName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 账本结构URL
     */
    private String previewUrl;

    /**
     * 标签
     */
    private List<TagTemplateBo> tagList;

    /**
     * 分类
     */
    private List<CategoryTemplateBo> categoryList;

    /**
     * 支付对象
     */
    private List<PayeeTemplateBo> payeeList;
}
