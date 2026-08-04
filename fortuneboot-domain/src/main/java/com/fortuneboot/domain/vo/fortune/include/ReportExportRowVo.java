package com.fortuneboot.domain.vo.fortune.include;

import com.fortuneboot.common.annotation.ExcelColumn;
import com.fortuneboot.common.annotation.ExcelSheet;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计报表导出行
 *
 * @author zhangchi118
 * @date 2026/8/4 19:44
 **/
@Data
@ExcelSheet(name = "统计报表")
public class ReportExportRowVo {

    @ExcelColumn(name = "报表模块")
    private String section;

    @ExcelColumn(name = "名称")
    private String name;

    @ExcelColumn(name = "收入")
    private BigDecimal income;

    @ExcelColumn(name = "支出")
    private BigDecimal expense;

    @ExcelColumn(name = "金额")
    private BigDecimal amount;

    @ExcelColumn(name = "百分比")
    private BigDecimal percent;

    @ExcelColumn(name = "数量")
    private Integer count;

    @ExcelColumn(name = "备注")
    private String remark;
}
