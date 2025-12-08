package com.fortuneboot.domain.vo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

    public FortuneBookVo(FortuneBookEntity entity, Map<Long, FortuneAccountEntity> accountMap) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 账本名称
     */
    private String bookName;

    /**
     * 默认币种
     */
    private String defaultCurrency;

    /**
     * 默认支出账户id
     */
    private Long defaultExpenseAccountId;

    /**
     * 默认支出账户名称
     */
    private String defaultExpenseAccountName;

    /**
     * 默认收入账户id
     */
    private Long defaultIncomeAccountId;

    /**
     * 默认收入账户名称
     */
    private String defaultIncomeAccountName;

    /**
     * 默认转出账户id
     */
    private Long defaultTransferOutAccountId;

    /**
     * 默认转出账户名称
     */
    private String defaultTransferOutAccountName;

    /**
     * 默认转入账户id
     */
    private Long defaultTransferInAccountId;

    /**
     * 默认转入账户名称
     */
    private String defaultTransferInAccountName;

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
