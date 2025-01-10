package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.common.utils.tree.AbstractTreeNode;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhangchi118
 * @date 2025/1/10 18:34
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryVo extends AbstractTreeNode {

    public FortuneCategoryVo(FortuneCategoryEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * id
     */
    private Long categoryId;

    /**
     * 名称
     */
    private String categoryName;

    /**
     * 类型
     * com.fortuneboot.common.enums.fortune.CategoryTypeEnum
     */
    private Integer categoryType;

    /**
     * 账本
     */
    private Long bookId;

    /**
     * 父级id
     */
    private Long parentId;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 回收站
      */
    private Boolean recycleBin;

    @Override
    public Long getId() {
        return categoryId;
    }
}
