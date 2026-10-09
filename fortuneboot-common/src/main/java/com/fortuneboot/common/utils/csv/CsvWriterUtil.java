package com.fortuneboot.common.utils.csv;

import java.io.IOException;
import java.io.OutputStream;

/**
 * CSV写出工具。
 * <p>统一RFC4180引号转义、UTF-8 BOM和Excel公式注入防护，禁止在业务服务中手写CSV细节。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public final class CsvWriterUtil {

    private static final String FORMULA_PREFIXES = "=+-@";
    private static final String FORMULA_ESCAPE_PREFIX = "'";

    private CsvWriterUtil() {
    }

    /**
     * 输出单个CSV单元格；null按空字符串处理。
     *
     * @param value              单元格文本
     * @param alwaysFormulaSafe 是否始终添加防公式注入前缀
     * @return 已加引号并转义的单元格
     */
    public static String cell(String value, boolean alwaysFormulaSafe) {
        String text = value == null ? "" : value;
        if (alwaysFormulaSafe || !text.isEmpty() && FORMULA_PREFIXES.indexOf(text.charAt(0)) >= 0) {
            text = FORMULA_ESCAPE_PREFIX + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    /**
     * 写出UTF-8 BOM，便于Excel识别中文CSV。
     */
    public static void writeUtf8Bom(OutputStream outputStream) throws IOException {
        outputStream.write(0xEF);
        outputStream.write(0xBB);
        outputStream.write(0xBF);
    }
}
