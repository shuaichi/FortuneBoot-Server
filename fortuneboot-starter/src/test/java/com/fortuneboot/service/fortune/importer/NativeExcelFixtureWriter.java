package com.fortuneboot.service.fortune.importer;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 供 scripts/native-excel-e2e.sh 调用，生成并校验原生 Excel 验收文件。
 */
public final class NativeExcelFixtureWriter {

    private static final int TITLE_COLUMN = 0;
    private static final int TIME_COLUMN = 1;
    private static final int TYPE_COLUMN = 2;
    private static final int CATEGORY_AMOUNT_COLUMN = 7;
    private static final int CONFIRM_COLUMN = 11;
    private static final int INCLUDE_COLUMN = 12;
    private static final String[] TEMPLATE_HEADERS = {
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
    private static final int HEADER_COUNT = TEMPLATE_HEADERS.length;

    private NativeExcelFixtureWriter() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 5 && "create".equals(args[0])) {
            createFixtures(Path.of(args[1]), Path.of(args[2]), Path.of(args[3]), Path.of(args[4]));
            return;
        }
        if (args.length == 2 && "verify-error".equals(args[0])) {
            verifyErrorWorkbook(Path.of(args[1]));
            return;
        }
        throw new IllegalArgumentException("参数应为：create <template.xlsx> <valid.xlsx> <valid.xls> <invalid.xlsx> 或 verify-error <error.xlsx>");
    }

    private static void createFixtures(Path templatePath, Path validXlsxPath, Path validXlsPath, Path invalidXlsxPath) throws Exception {
        try (InputStream inputStream = Files.newInputStream(templatePath);
             Workbook template = WorkbookFactory.create(inputStream)) {
            verifyTemplate(template);
            writeValidWorkbook(template, validXlsxPath, "native-e2e-xlsx");
            writeInvalidWorkbook(template, invalidXlsxPath);
        }
        writeValidXlsWorkbook(validXlsPath, "native-e2e-xls");
    }

    private static void verifyTemplate(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        if (!"账单导入".equals(sheet.getSheetName())) {
            throw new IllegalStateException("模板工作表名称错误：" + sheet.getSheetName());
        }
        Row header = sheet.getRow(0);
        if (header == null || header.getLastCellNum() != HEADER_COUNT) {
            throw new IllegalStateException("模板表头数量错误");
        }
        for (int column = 0; column < HEADER_COUNT; column++) {
            if (!TEMPLATE_HEADERS[column].equals(header.getCell(column).getStringCellValue())) {
                throw new IllegalStateException("模板表头错误，列 " + column);
            }
        }
    }

    private static void verifyErrorWorkbook(Path errorPath) throws Exception {
        try (InputStream inputStream = Files.newInputStream(errorPath);
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            Row row = sheet.getRow(1);
            if (header == null || row == null || header.getLastCellNum() <= HEADER_COUNT) {
                throw new IllegalStateException("错误 Excel 缺少错误原因列或错误数据");
            }
            Cell errorCell = row.getCell(header.getLastCellNum() - 1);
            if (errorCell == null || errorCell.getStringCellValue().isBlank()) {
                throw new IllegalStateException("错误 Excel 未包含行级错误信息");
            }
        }
    }

    private static void writeValidWorkbook(Workbook workbook, Path outputPath, String title) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        removeDataRows(sheet);
        writeValidRow(sheet.createRow(1), title);
        write(workbook, outputPath);
    }

    private static void writeInvalidWorkbook(Workbook workbook, Path outputPath) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        removeDataRows(sheet);
        Row row = sheet.createRow(1);
        row.createCell(TITLE_COLUMN).setCellValue("native-e2e-invalid");
        row.createCell(TIME_COLUMN).setCellValue("2026-08-12 09:30:00");
        row.createCell(TYPE_COLUMN).setCellValue("支出");
        row.createCell(CATEGORY_AMOUNT_COLUMN).setCellValue("不存在的分类:15.50");
        row.createCell(CONFIRM_COLUMN).setCellValue("是");
        row.createCell(INCLUDE_COLUMN).setCellValue("是");
        write(workbook, outputPath);
    }

    private static void writeValidXlsWorkbook(Path outputPath, String title) throws Exception {
        try (Workbook workbook = new HSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("账单导入");
            Row header = sheet.createRow(0);
            for (int column = 0; column < HEADER_COUNT; column++) {
                header.createCell(column).setCellValue(TEMPLATE_HEADERS[column]);
            }
            writeValidRow(sheet.createRow(1), title);
            write(workbook, outputPath);
        }
    }

    private static void writeValidRow(Row row, String title) {
        row.createCell(TITLE_COLUMN).setCellValue(title);
        row.createCell(TIME_COLUMN).setCellValue("2026-08-12 09:30:00");
        row.createCell(TYPE_COLUMN).setCellValue("支出");
        row.createCell(CATEGORY_AMOUNT_COLUMN).setCellValue("维持类:15.50");
        row.createCell(CONFIRM_COLUMN).setCellValue("是");
        row.createCell(INCLUDE_COLUMN).setCellValue("是");
    }

    private static void removeDataRows(Sheet sheet) {
        for (int index = sheet.getLastRowNum(); index > 0; index--) {
            sheet.removeRow(sheet.getRow(index));
        }
    }

    private static void write(Workbook workbook, Path outputPath) throws Exception {
        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            workbook.write(outputStream);
        }
    }
}
