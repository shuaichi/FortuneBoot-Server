package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneBillImportRowBuilderTest {

    private final FortuneBillImportRowBuilder rowBuilder = new FortuneBillImportRowBuilder(new FortuneBillImportConverter());

    @Test
    @DisplayName("跳过全空行并保留 Excel 实际行号")
    void buildRows_blankRowsSkipped_keepsExcelRowNumber() {
        FortuneBillImportExcelVo blank = new FortuneBillImportExcelVo();
        FortuneBillImportExcelVo invalid = new FortuneBillImportExcelVo();
        invalid.setTitle("午餐");
        invalid.setTradeTime(LocalDateTime.of(2026, 6, 24, 12, 0));
        invalid.setBillType("借出");

        List<FortuneBillImportRow> rows = rowBuilder.buildRows(List.of(blank, invalid), context());

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getRowNum()).isEqualTo(3);
        assertThat(rows.getFirst().getErrors()).contains("第3行：流水类型【借出】暂不支持导入");
    }

    private FortuneBillImportContext context() {
        FortuneBookEntity book = new FortuneBookEntity();
        book.setBookId(1L);
        FortuneBillImportContext context = new FortuneBillImportContext();
        context.setBookModel(new FortuneBookModel(book, null));
        return context;
    }
}
