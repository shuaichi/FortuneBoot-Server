package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneGroupEntity;
import lombok.Data;

/**
 * 分组BVO
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/9 17:14
 **/
@Data
public class FortuneGroupVo {

    public FortuneGroupVo(FortuneGroupEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 主键
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 默认币种
     */
    private String defaultCurrency;

    /**
     * 权限名称
     */
    private String roleTypeDesc;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 默认账本id
     */
    private Long defaultBookId;

    /**
     * 默认账本名称
     */
    private String defaultBookName;

    /**
     * 备注
     */
    private String remark;
}
