package com.fortuneboot.common.utils.csv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CSV写出工具")
class CsvWriterUtilTest {

    @Test
    @DisplayName("单元格使用RFC4180引号并转义双引号")
    void cell_quotesAndEscapesValue() {
        assertThat(CsvWriterUtil.cell("a\"b", false)).isEqualTo("\"a\"\"b\"");
        assertThat(CsvWriterUtil.cell("a,b", false)).isEqualTo("\"a,b\"");
    }

    @Test
    @DisplayName("null与空字符串输出为空单元格")
    void cell_treatsNullAndEmptyAsEmpty() {
        assertThat(CsvWriterUtil.cell(null, false)).isEqualTo("\"\"");
        assertThat(CsvWriterUtil.cell("", false)).isEqualTo("\"\"");
    }

    @Test
    @DisplayName("以=+-@开头的文本防Excel公式注入")
    void cell_protectsFormulaPrefix() {
        assertThat(CsvWriterUtil.cell("=1+1", false)).isEqualTo("\"'=1+1\"");
        assertThat(CsvWriterUtil.cell("+1", false)).isEqualTo("\"'+1\"");
        assertThat(CsvWriterUtil.cell("-1", false)).isEqualTo("\"'-1\"");
        assertThat(CsvWriterUtil.cell("@cmd", false)).isEqualTo("\"'@cmd\"");
    }

    @Test
    @DisplayName("普通文本不加防公式前缀，SUM等合法文本保持原样")
    void cell_keepsNormalTextUnchanged() {
        assertThat(CsvWriterUtil.cell("SUM", false)).isEqualTo("\"SUM\"");
        assertThat(CsvWriterUtil.cell("张三", false)).isEqualTo("\"张三\"");
    }

    @Test
    @DisplayName("可指定单元格始终防公式注入")
    void cell_canAlwaysBeFormulaSafe() {
        assertThat(CsvWriterUtil.cell("abc", true)).isEqualTo("\"'abc\"");
    }

    @Test
    @DisplayName("UTF-8 BOM逐字节写出为EF BB BF")
    void bom_writesUtf8Bytes() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        CsvWriterUtil.writeUtf8Bom(outputStream);

        assertThat(outputStream.toByteArray()).containsExactly((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
    }
}
