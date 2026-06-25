package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangchi118
 */
@Component
@RequiredArgsConstructor
class FortuneBillImportRowBuilder {

    private final FortuneBillImportConverter converter;

    List<FortuneBillImportRow> buildRows(List<FortuneBillImportExcelVo> sources, FortuneBillImportContext context) {
        List<FortuneBillImportRow> rows = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            FortuneBillImportExcelVo source = sources.get(i);
            if (converter.isBlankRow(source)) {
                continue;
            }
            FortuneBillImportRow row = new FortuneBillImportRow(i + 2, source);
            converter.convert(row, context);
            rows.add(row);
        }
        if (CollectionUtils.isEmpty(rows)) {
            throw new ApiException(ErrorCode.Business.UPLOAD_IMPORT_EXCEL_FAILED, "导入文件没有有效账单数据");
        }
        return rows;
    }
}
