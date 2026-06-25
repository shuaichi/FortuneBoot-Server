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

    @ExcelColumn(name = "标题（必填，50字以内）")
    private String title;

    @ExcelColumn(name = "交易时间（必填，yyyy-MM-dd HH:mm:ss）")
    private LocalDateTime tradeTime;

    @ExcelColumn(name = "流水类型（必填，支出/收入/转账/垫付/报销）")
    private String billType;

    @ExcelColumn(name = "账户（转账必填，其他可空）")
    private String account;

    @ExcelColumn(name = "转入账户（转账必填）")
    private String toAccount;

    @ExcelColumn(name = "单据ID（垫付/报销必填）")
    private Long orderId;

    @ExcelColumn(name = "交易对象（选填，填名称）")
    private String payee;

    @ExcelColumn(name = "分类金额（支出/收入/垫付/报销必填，分类:金额；分类:金额）")
    private String categoryAmounts;

    @ExcelColumn(name = "金额（转账必填，其他可空）")
    private BigDecimal amount;

    @ExcelColumn(name = "标签（选填，标签1；标签2）")
    private String tags;

    @ExcelColumn(name = "成员（选填，成员1；成员2）")
    private String members;

    @ExcelColumn(name = "是否确认（选填，是/否/true/false/1/0）")
    private String confirm;

    @ExcelColumn(name = "是否统计（选填，是/否/true/false/1/0）")
    private String include;

    @ExcelColumn(name = "备注（选填，512字以内）")
    private String remark;

    @ExcelColumn(name = "附加费用（选填，仅支出/转账，类型:金额:账户方向:分类:备注）")
    private String extras;

    @ExcelColumn(name = "附件（暂不支持，请留空）")
    private String files;
}
