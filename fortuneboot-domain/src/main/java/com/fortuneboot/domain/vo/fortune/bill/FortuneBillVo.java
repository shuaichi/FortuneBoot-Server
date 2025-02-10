package com.fortuneboot.domain.vo.fortune.bill;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.vo.fortune.FortuneCategoryVo;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 23:01
 **/
@Data
public class FortuneBillVo {

    public FortuneBillVo(FortuneBillEntity entity){
        if (ObjectUtil.isNotEmpty(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    public FortuneBillVo(FortuneBillBo bo){
        if (ObjectUtil.isNotEmpty(bo)){
            BeanUtil.copyProperties(bo,this);
        }
        if (CollectionUtils.isNotEmpty(bo.getTagList())){
            List<FortuneTagVo> tagList = bo.getTagList().stream().map(FortuneTagVo::new).toList();
            this.setTagList(tagList);
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
     *标题
     */
    private String title;

    /**
     *交易时间
     */
    private LocalDateTime tradeTime;

    /**
     *账户id
     */
    private Long accountId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     *金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     *汇率转换后的金额
     */
    private BigDecimal convertedAmount;

    /**
     *交易对象
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
     *转账到的账户
     */
    private Long toAccountId;

    /**
     * 转账到的账户
     */
    private String toAccountName;

    /**
     *是否确认
     */
    private Boolean confirm;

    /**
     *是否统计
     */
    private Boolean include;

    /**
     *备注
     */
    private String remark;

    /**
     * 分类
     */
    private List<BillCategoryAmountVo> categoryAmountPair;

    /**
     * 标签
     */
    private List<FortuneTagVo> tagList;
}
