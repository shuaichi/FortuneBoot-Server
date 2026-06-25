package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        assertThat(writer.writeErrorFile(java.util.List.of(row))).isNotEmpty();
        assertThat(writer.writeTemplate()).isNotEmpty();
    }
}
