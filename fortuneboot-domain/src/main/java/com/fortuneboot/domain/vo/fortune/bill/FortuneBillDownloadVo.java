package com.fortuneboot.domain.vo.fortune.bill;

import com.fortuneboot.common.annotation.ExcelColumn;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhangchi118
 * @date 2025/4/29 10:51
 **/
@Data
public class FortuneBillDownloadVo {

    public FortuneBillDownloadVo(FortuneBillBo bo) {
        if (Objects.nonNull(bo)) {
            BeanUtils.copyProperties(bo, this);
        }
        this.billTypeDesc = BillTypeEnum.getDescByValue(bo.getBillType());
        this.categories = Optional.ofNullable(bo.getCategoryAmountPair())
                .orElse(new ArrayList<>()).stream()
                .map(BillCategoryAmountVo::getCategoryName)
                .collect(Collectors.joining("， "));
        this.tags = Optional.ofNullable(bo.getTagList())
                .orElse(new ArrayList<>()).stream()
                .map(FortuneTagEntity::getTagName)
                .collect(Collectors.joining("， "));
        this.confirm = bo.getConfirm() ? "是" : "否";
        this.include = bo.getInclude() ? "是" : "否";
        if (Objects.equals(BillTypeEnum.TRANSFER.getValue(),bo.getBillType())){
            this.accountName = bo.getAccountName() + " -> " + bo.getToAccountName();
        }
    }

    /**
     * id
     */
    @ExcelColumn(name = "账单ID")
    private Long billId;

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
     * 账户
     */
    @ExcelColumn(name = "账户")
    private String accountName;

    /**
     * 币种
     */
    @ExcelColumn(name = "币种")
    private String currencyCode;

    /**
     * 流水类型
     *
     * @see BillTypeEnum
     */
    @ExcelColumn(name = "流水类型")
    private String billTypeDesc;

    /**
     * 汇率转换后的金额
     */
    @ExcelColumn(name = "金额")
    private BigDecimal convertedAmount;

    /**
     * 交易对象
     */
    @ExcelColumn(name = "交易对象")
    private String payeeName;

    /**
     * 分类
     */
    @ExcelColumn(name = "分类")
    private String categories;

    /**
     * 标签
     */
    @ExcelColumn(name = "标签")
    private String tags;

    /**
     * 是否确认
     */
    @ExcelColumn(name = "是否确认")
    private String confirm;

    /**
     * 是否统计
     */
    @ExcelColumn(name = "是否统计")
    private String include;

    /**
     * 备注
     */
    @ExcelColumn(name = "备注")
    private String remark;

}
