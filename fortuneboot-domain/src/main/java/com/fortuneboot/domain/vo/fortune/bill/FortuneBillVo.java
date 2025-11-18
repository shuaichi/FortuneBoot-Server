package com.fortuneboot.domain.vo.fortune.bill;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fortuneboot.common.annotation.ExcelColumn;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
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

    public FortuneBillVo(FortuneBillBo bo){
        if (ObjectUtil.isNotEmpty(bo)){
            BeanUtil.copyProperties(bo,this);
        }
        if (CollectionUtils.isNotEmpty(bo.getTagList())){
            List<FortuneTagVo> tags = bo.getTagList().stream().map(FortuneTagVo::new).toList();
            this.setTagList(tags);
        }
        this.setHasFile(bo.getHasFile());
    }
    /**
     * id
     */
    @ExcelColumn(name = "账单ID")
    private Long billId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 账本名称
     */
    @ExcelColumn(name = "账本")
    private String bookName;

    /**
     * 标题
     */
    @ExcelColumn(name = "标题")
    private String title;

    /**
     * 交易时间
     */
    @ExcelColumn(name = "交易时间")
    private LocalDateTime tradeTime;

    /**
     * 账户id
     */
    private Long accountId;

    /**
     * 单据ID
     */
    private Long orderId;

    /**
     * 账户
     */
    @ExcelColumn(name = "账户")
    private String accountName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    @ExcelColumn(name = "币种")
    private String currencyCode;

    /**
     * 被转入账户金额币种
     */
    private String toCurrencyCode;

    /**
     * 汇率转换后的金额
     */
    @ExcelColumn(name = "金额")
    private BigDecimal convertedAmount;

    /**
     * 交易对象
     */
    private Long payeeId;

    /**
     * 交易对象
     */
    @ExcelColumn(name = "交易对象")
    private String payeeName;

    /**
     * 流水类型
     * @see BillTypeEnum
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

    /**
     * 是否存在附件
     * JSON字段：hasFile；默认false，服务层批量判定后置true
     */
    private Boolean hasFile = false;
}
