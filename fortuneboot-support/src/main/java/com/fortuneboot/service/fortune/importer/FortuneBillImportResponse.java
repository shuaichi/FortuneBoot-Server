package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportResultVo;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zhangchi118
 */
@Data
@AllArgsConstructor
public class FortuneBillImportResponse {

    private boolean success;

    private FortuneBillImportResultVo result;

    private byte[] errorFile;
}
