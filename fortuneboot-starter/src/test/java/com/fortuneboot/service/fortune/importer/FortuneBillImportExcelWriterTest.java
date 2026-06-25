package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneBillImportExcelWriterTest {

    private final FortuneBillImportExcelWriter writer = new FortuneBillImportExcelWriter();

    @Test
    @DisplayName("错误 Excel 和模板均能生成非空字节")
    void writeExcel_generatesBytes() {
        FortuneBillImportExcelVo source = new FortuneBillImportExcelVo();
        source.setTitle("午餐");
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);
        row.addError("第2行：错误");

        assertThat(writer.writeErrorFile(List.of(row))).isNotEmpty();
        assertThat(writer.writeTemplate()).isNotEmpty();
    }

    @Test
    @DisplayName("模板表头包含必填和格式说明")
    void writeTemplate_headerContainsRequiredAndFormatHints() throws Exception {
        byte[] bytes = writer.writeTemplate();

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var header = workbook.getSheetAt(0).getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("标题（必填，50字以内）");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("交易时间（必填，yyyy-MM-dd HH:mm:ss）");
            assertThat(header.getCell(3).getStringCellValue()).isEqualTo("账户（转账必填，其他可空）");
            assertThat(header.getCell(7).getStringCellValue()).contains("分类:金额");
            assertThat(header.getCell(15).getStringCellValue()).isEqualTo("附件（暂不支持，请留空）");
        }
    }
}
