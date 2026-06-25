package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportErrorVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangchi118
 */
@Data
class FortuneBillImportRow {

    private final Integer rowNum;

    private final FortuneBillImportExcelVo source;

    private final List<String> errors = new ArrayList<>();

    private FortuneBillAddCommand command;

    private Long billId;

    void addError(String error) {
        errors.add(error);
    }

    boolean hasError() {
        return !errors.isEmpty();
    }

    FortuneBillImportErrorVo toErrorVo() {
        FortuneBillImportErrorVo vo = new FortuneBillImportErrorVo();
        BeanUtils.copyProperties(source, vo);
        vo.setErrorReason(String.join("；", errors));
        return vo;
    }
}
