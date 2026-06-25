package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportResultVo;
import com.fortuneboot.factory.fortune.factory.FortuneBookFactory;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author zhangchi118
 */
@Service
@RequiredArgsConstructor
public class FortuneBillImportService {

    private final FortuneBookFactory fortuneBookFactory;
    private final FortuneBillImportFileValidator fileValidator;
    private final FortuneBillImportContextLoader contextLoader;
    private final FortuneBillImportRowBuilder rowBuilder;
    private final FortuneBillImportPersistService persistService;
    private final FortuneBillImportExcelWriter excelWriter;

    public FortuneBillImportResponse importByExcel(Long bookId, MultipartFile file) {
        fileValidator.validate(file);
        FortuneBookModel bookModel = fortuneBookFactory.loadById(bookId);
        bookModel.checkNotInRecycleBin();
        List<FortuneBillImportExcelVo> sources = CustomExcelUtil.readFromRequest(FortuneBillImportExcelVo.class, file, FortuneBillImportFileValidator.MAX_IMPORT_ROWS);
        FortuneBillImportContext context = contextLoader.load(bookModel);
        List<FortuneBillImportRow> rows = rowBuilder.buildRows(sources, context);
        List<FortuneBillImportRow> errorRows = rows.stream().filter(FortuneBillImportRow::hasError).toList();
        if (CollectionUtils.isNotEmpty(errorRows)) {
            return new FortuneBillImportResponse(false, null, excelWriter.writeErrorFile(errorRows));
        }
        FortuneBillImportResultVo result;
        try {
            result = persistService.persistRows(rows, context);
        } catch (ApiException e) {
            List<FortuneBillImportRow> persistErrorRows = rows.stream().filter(FortuneBillImportRow::hasError).toList();
            if (CollectionUtils.isNotEmpty(persistErrorRows)) {
                return new FortuneBillImportResponse(false, null, excelWriter.writeErrorFile(persistErrorRows));
            }
            throw e;
        }
        result.setTotalCount(rows.size());
        result.setSuccessCount(rows.size());
        result.setFailCount(0);
        result.setMessage("导入成功，共导入" + rows.size() + "条账单");
        return new FortuneBillImportResponse(true, result, null);
    }

    public byte[] template() {
        return excelWriter.writeTemplate();
    }
}
