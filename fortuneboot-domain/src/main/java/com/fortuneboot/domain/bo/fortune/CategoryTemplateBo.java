package com.fortuneboot.domain.bo.fortune;

import lombok.Data;

/**
 * 分类模板Bo
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 00:21
 **/
@Data
public class CategoryTemplateBo {

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 名称
     */
    private String categoryName;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 类型
     */
    private Integer categoryType;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
