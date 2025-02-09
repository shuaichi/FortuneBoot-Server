package com.fortuneboot.domain.bo.fortune;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/2/9 21:51
 **/
@Data
public class FortuneBillBo {

    public FortuneBillBo(FortuneBillEntity entity) {
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    /**
     * id
     */
    private Long billId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 账本名称
     */
    private String bookName;

    /**
     * 标题
     */
    private String title;

    /**
     * 交易时间
     */
    private LocalDateTime tradeTime;

    /**
     * 账户id
     */
    private Long accountId;

    /**
     * 账户
     */
    private String accountName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 汇率转换后的金额
     */
    private BigDecimal convertedAmount;

    /**
     * 交易对象
     */
    private Long payeeId;

    /**
     * 交易对象
     */
    private String payeeName;

    /**
     * 流水类型
     * com.fortuneboot.common.enums.fortune.BillTypeEnum
     */
    private Integer billType;

    /**
     * 转账到的账户
     */
    private Long toAccountId;

    /**
     * 转账到的账户
     */
    private String toAccountName;

    /**
     * 是否确认
     */
    private Boolean confirm;

    /**
     * 是否统计
     */
    private Boolean include;

    /**
     * 备注
     */
    private Boolean remark;

    /**
     * 分类
     */
    private List<FortuneCategoryEntity> categoryList;

    /**
     * 标签
     */
    private List<FortuneTagEntity> tagList;

}
