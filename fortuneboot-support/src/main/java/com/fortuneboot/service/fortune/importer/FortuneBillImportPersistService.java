package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangchi118
 */
@Service
@RequiredArgsConstructor
public class FortuneBillImportPersistService {

    private final FortuneBillImportRelationBatch relationBatch;
    private final FortuneBillImportRowPersistor rowPersistor;

    @Transactional(rollbackFor = Exception.class)
    public FortuneBillImportResultVo persistRows(List<FortuneBillImportRow> rows, FortuneBillImportContext importContext) {
        FortuneBillImportRelationBatch.Relations relations = relationBatch.create();
        List<Long> billIds = new ArrayList<>();
        try {
            for (FortuneBillImportRow row : rows) {
                rowPersistor.persist(row, importContext, relations, billIds);
            }
            relationBatch.save(relations);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e, ErrorCode.Business.UPLOAD_IMPORT_EXCEL_FAILED, e.getMessage());
        }
        return new FortuneBillImportResultVo(null, null, null, billIds, null);
    }
}
