package com.fortuneboot.domain.vo.fortune.bill;

import com.fortuneboot.common.annotation.ExcelColumn;
import com.fortuneboot.common.annotation.ExcelSheet;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zhangchi118
 */
@Data
@ExcelSheet(name = "账单导入")
public class FortuneBillImportExcelVo {

    @ExcelColumn(name = "标题")
    private String title;

    @ExcelColumn(name = "交易时间")
    private LocalDateTime tradeTime;

    @ExcelColumn(name = "流水类型")
    private String billType;

    @ExcelColumn(name = "账户")
    private String account;

    @ExcelColumn(name = "转入账户")
    private String toAccount;

    @ExcelColumn(name = "单据ID")
    private Long orderId;

    @ExcelColumn(name = "交易对象")
    private String payee;

    @ExcelColumn(name = "分类金额")
    private String categoryAmounts;

    @ExcelColumn(name = "金额")
    private BigDecimal amount;

    @ExcelColumn(name = "标签")
    private String tags;

    @ExcelColumn(name = "成员")
    private String members;

    @ExcelColumn(name = "是否确认")
    private String confirm;

    @ExcelColumn(name = "是否统计")
    private String include;

    @ExcelColumn(name = "备注")
    private String remark;

    @ExcelColumn(name = "附加费用")
    private String extras;

    @ExcelColumn(name = "附件")
    private String files;
}
