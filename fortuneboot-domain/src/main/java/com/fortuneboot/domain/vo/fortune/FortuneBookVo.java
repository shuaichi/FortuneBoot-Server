package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import lombok.Data;

/**
 * @author zhangchi118
 * @date 2024/12/2 11:13
 **/
@Data
public class FortuneBookVo {

    /**
     * 构造方法
     *
     * @param entity
     */
    public FortuneBookVo(FortuneBookEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 账本id
     */
    private Long bookId;

    /**
     *
     */
    private Long groupId;

    /**
     *
     */
    private String bookName;

    /**
     *
     */
    private String defaultCurrency;

    /**
     *
     */
    private Long defaultExpenseAccountId;

    /**
     *
     */
    private Long defaultIncomeAccountId;

    /**
     *
     */
    private Long defaultTransferOutAccountId;

    /**
     *
     */
    private Long defaultTransferInAccountId;

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
}
