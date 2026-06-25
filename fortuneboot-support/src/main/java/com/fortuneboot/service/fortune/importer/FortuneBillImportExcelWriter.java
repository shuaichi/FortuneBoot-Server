package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportErrorVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhangchi118
 */
@Component
class FortuneBillImportExcelWriter {

    private static final String[] HEADERS = {
            "标题（必填，50字以内）",
            "交易时间（必填，yyyy-MM-dd HH:mm:ss）",
            "流水类型（必填，支出/收入/转账/垫付/报销）",
            "账户（转账必填，其他可空）",
            "转入账户（转账必填）",
            "单据ID（垫付/报销必填）",
            "交易对象（选填，填名称）",
            "分类金额（支出/收入/垫付/报销必填，分类:金额；分类:金额）",
            "金额（转账必填，其他可空）",
            "标签（选填，标签1；标签2）",
            "成员（选填，成员1；成员2）",
            "是否确认（选填，是/否）",
            "是否统计（选填，是/否）",
            "备注（选填，512字以内）",
            "附加费用（选填，仅支出/转账，类型:金额:账户方向:分类:备注）",
            "附件（暂不支持，请留空）"
    };

    private static final int[] CONDITION_REQUIRED_COLUMNS = {3, 4, 5, 7, 8};

    byte[] writeErrorFile(List<FortuneBillImportRow> rows) {
        List<FortuneBillImportErrorVo> errorRows = rows.stream().map(FortuneBillImportRow::toErrorVo).toList();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CustomExcelUtil.writeToOutputStream(errorRows, FortuneBillImportErrorVo.class, outputStream);
        return outputStream.toByteArray();
    }

    byte[] writeTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("账单导入");
            sheet.createFreezePane(0, 1);
            writeHeader(workbook, sheet);
            writeExampleRow(sheet);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("生成账单导入模板失败", e);
        }
    }

    private void writeHeader(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle requiredStyle = createHeaderStyle(workbook, IndexedColors.LIGHT_ORANGE);
        CellStyle conditionRequiredStyle = createHeaderStyle(workbook, IndexedColors.LIGHT_YELLOW);
        CellStyle optionalStyle = createHeaderStyle(workbook, IndexedColors.LIGHT_GREEN);
        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle(i, requiredStyle, conditionRequiredStyle, optionalStyle));
            sheet.setColumnWidth(i, Math.min(HEADERS[i].length() + 4, 40) * 384);
        }
    }

    private CellStyle headerStyle(int columnIndex, CellStyle requiredStyle, CellStyle conditionRequiredStyle, CellStyle optionalStyle) {
        if (columnIndex <= 2) {
            return requiredStyle;
        }
        for (int conditionRequiredColumn : CONDITION_REQUIRED_COLUMNS) {
            if (columnIndex == conditionRequiredColumn) {
                return conditionRequiredStyle;
            }
        }
        return optionalStyle;
    }

    private CellStyle createHeaderStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private void writeExampleRow(Sheet sheet) {
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("示例-午餐");
        row.createCell(1).setCellValue(LocalDateTime.now().withNano(0).toString().replace('T', ' '));
        row.createCell(2).setCellValue("支出");
        row.createCell(3).setCellValue("现金");
        row.createCell(6).setCellValue("京东");
        row.createCell(7).setCellValue("餐饮:35.50；交通:12");
        row.createCell(8).setCellValue("47.50");
        row.createCell(9).setCellValue("外卖；工作餐");
        row.createCell(10).setCellValue("张三；李四");
        row.createCell(11).setCellValue("是");
        row.createCell(12).setCellValue("是");
        row.createCell(13).setCellValue("示例行，请按实际数据修改或删除");
        row.createCell(14).setCellValue("手续费:2.50:转出账户:手续费:备注");
    }
}
