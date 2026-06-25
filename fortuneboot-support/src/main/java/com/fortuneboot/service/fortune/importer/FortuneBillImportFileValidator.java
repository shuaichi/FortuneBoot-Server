package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * @author zhangchi118
 */
@Component
class FortuneBillImportFileValidator {

    static final int MAX_IMPORT_ROWS = 5000;

    private static final long MAX_IMPORT_FILE_SIZE = 10 * 1024 * 1024;

    void validate(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_IS_EMPTY);
        }
        if (file.getSize() > MAX_IMPORT_FILE_SIZE) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_SIZE_EXCEED_MAX_SIZE, MAX_IMPORT_FILE_SIZE / 1024 / 1024);
        }
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!StringUtils.equalsAnyIgnoreCase(extension, "xls", "xlsx")) {
            throw new ApiException(ErrorCode.Business.UPLOAD_FILE_TYPE_NOT_ALLOWED, "xls,xlsx");
        }
    }
}
