package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneBillImportExcelWriterTest {

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

    private final FortuneBillImportExcelWriter writer = new FortuneBillImportExcelWriter();

    @Test
    @DisplayName("错误 Excel 和模板均能生成可读取的字节")
    void writeExcel_generatesReadableBytes() throws Exception {
        FortuneBillImportExcelVo source = new FortuneBillImportExcelVo();
        source.setTitle("午餐");
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);
        row.addError("第2行：错误");

        byte[] errorFile = writer.writeErrorFile(List.of(row));
        byte[] template = writer.writeTemplate();

        assertThat(errorFile).isNotEmpty();
        assertThat(template).isNotEmpty();
        try (var errorWorkbook = WorkbookFactory.create(new ByteArrayInputStream(errorFile));
             var templateWorkbook = WorkbookFactory.create(new ByteArrayInputStream(template))) {
            assertThat(errorWorkbook.getSheetAt(0).getLastRowNum()).isEqualTo(1);
            assertThat(templateWorkbook.getSheetAt(0).getSheetName()).isEqualTo("账单导入");
        }
    }

    @Test
    @DisplayName("模板表头包含完整列顺序和格式说明")
    void writeTemplate_containsAllHeadersInExpectedOrder() throws Exception {
        byte[] bytes = writer.writeTemplate();

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            var header = sheet.getRow(0);

            assertThat(sheet.getPaneInformation().isFreezePane()).isTrue();
            assertThat(header.getLastCellNum()).isEqualTo((short) TEMPLATE_HEADERS.length);
            for (int column = 0; column < TEMPLATE_HEADERS.length; column++) {
                assertThat(header.getCell(column).getStringCellValue()).isEqualTo(TEMPLATE_HEADERS[column]);
            }
        }
    }

    @Test
    @DisplayName("模板短表头和 DTO 完整表头均可解析确认与统计字段")
    void readFromInputStream_mapsTemplateAndFullBooleanHeaders() throws Exception {
        for (String[] headers : List.of(TEMPLATE_HEADERS, fullHeaders())) {
            List<FortuneBillImportExcelVo> rows = CustomExcelUtil.readFromInputStream(
                    FortuneBillImportExcelVo.class, new ByteArrayInputStream(createWorkbook(new XSSFWorkbook(), headers)));

            assertThat(rows).singleElement().satisfies(row -> {
                assertThat(row.getTitle()).isEqualTo("native-excel-contract");
                assertThat(row.getTradeTime()).isEqualTo(LocalDateTime.of(2026, 8, 12, 9, 30));
                assertThat(row.getBillType()).isEqualTo("支出");
                assertThat(row.getCategoryAmounts()).isEqualTo("维持类:15.50");
                assertThat(row.getConfirm()).isEqualTo("是");
                assertThat(row.getInclude()).isEqualTo("否");
            });
        }
    }

    @Test
    @DisplayName("xls 和 xlsx 均可解析下载模板格式")
    void readFromInputStream_supportsXlsAndXlsx() throws Exception {
        for (Workbook workbook : List.of(new HSSFWorkbook(), new XSSFWorkbook())) {
            List<FortuneBillImportExcelVo> rows = CustomExcelUtil.readFromInputStream(
                    FortuneBillImportExcelVo.class, new ByteArrayInputStream(createWorkbook(workbook, TEMPLATE_HEADERS)));

            assertThat(rows).singleElement().satisfies(row -> {
                assertThat(row.getTitle()).isEqualTo("native-excel-contract");
                assertThat(row.getConfirm()).isEqualTo("是");
                assertThat(row.getInclude()).isEqualTo("否");
            });
        }
    }

    private String[] fullHeaders() {
        String[] headers = TEMPLATE_HEADERS.clone();
        headers[11] = "是否确认（选填，是/否/true/false/1/0）";
        headers[12] = "是否统计（选填，是/否/true/false/1/0）";
        return headers;
    }

    private byte[] createWorkbook(Workbook workbook, String[] headers) throws Exception {
        try (workbook; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("账单导入");
            var header = sheet.createRow(0);
            var row = sheet.createRow(1);
            for (int column = 0; column < headers.length; column++) {
                header.createCell(column).setCellValue(headers[column]);
            }
            row.createCell(0).setCellValue("native-excel-contract");
            row.createCell(1).setCellValue("2026-08-12 09:30:00");
            row.createCell(2).setCellValue("支出");
            row.createCell(7).setCellValue("维持类:15.50");
            row.createCell(11).setCellValue("是");
            row.createCell(12).setCellValue("否");
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
