package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FortuneBillImportFileValidatorTest {

    private final FortuneBillImportFileValidator validator = new FortuneBillImportFileValidator();

    @Test
    @DisplayName("允许 xls 和 xlsx 文件")
    void validate_excelFile_passes() {
        MockMultipartFile file = new MockMultipartFile("file", "账单.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1});

        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("空文件直接拒绝")
    void validate_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "账单.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{});

        assertThatThrownBy(() -> validator.validate(file)).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("非 Excel 后缀直接拒绝")
    void validate_nonExcelFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "账单.txt", "text/plain", new byte[]{1});

        assertThatThrownBy(() -> validator.validate(file)).isInstanceOf(ApiException.class);
    }
}
