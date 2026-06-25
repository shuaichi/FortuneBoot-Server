package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportErrorVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

/**
 * @author zhangchi118
 */
@Component
class FortuneBillImportExcelWriter {

    byte[] writeErrorFile(List<FortuneBillImportRow> rows) {
        List<FortuneBillImportErrorVo> errorRows = rows.stream().map(FortuneBillImportRow::toErrorVo).toList();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CustomExcelUtil.writeToOutputStream(errorRows, FortuneBillImportErrorVo.class, outputStream);
        return outputStream.toByteArray();
    }

    byte[] writeTemplate() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CustomExcelUtil.writeToOutputStream(Collections.singletonList(new FortuneBillImportExcelVo()), FortuneBillImportExcelVo.class, outputStream);
        return outputStream.toByteArray();
    }
}
