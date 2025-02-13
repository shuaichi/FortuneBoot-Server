package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import lombok.Data;

/**
 * 交易对象
 * @author zhangchi118
 * @date 2025/1/10 16:25
 **/
@Data
public class FortunePayeeVo {

    public FortunePayeeVo(FortunePayeeEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * id
     */
    private Long payeeId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 交易对象名称
     */
    private String payeeName;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 可支出
     */
    private Boolean canExpense;

    /**
     * 可收入
     */
    private Boolean canIncome;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 备注
     */
    private String remark;

}
