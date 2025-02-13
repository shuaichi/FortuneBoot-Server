package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.common.utils.tree.AbstractTreeNode;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签Vo
 *
 * @author zhangchi118
 * @date 2024/12/11 16:26
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneTagVo extends AbstractTreeNode {

    public FortuneTagVo(FortuneTagEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * id
     */
    private Long tagId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 可转账
     */
    private Boolean canTransfer;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    @Override
    public Long getId() {
        return tagId;
    }
}
