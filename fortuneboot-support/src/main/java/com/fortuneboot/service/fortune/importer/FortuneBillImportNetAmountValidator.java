package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author zhangchi118
 */
@Component
class FortuneBillImportNetAmountValidator {

    void validate(FortuneBillAddCommand command, FortuneBillModel billModel) {
        if (CollectionUtils.isEmpty(command.getExtras())) {
            return;
        }
        if (Objects.equals(command.getBillType(), BillTypeEnum.EXPENSE.getValue())) {
            BigDecimal fee = sumExtras(command, 1, null);
            BigDecimal discount = sumExtras(command, 2, null);
            if (billModel.getAmount().add(fee).subtract(discount).compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(ErrorCode.Business.UPLOAD_IMPORT_EXCEL_FAILED, "优惠金额不能大于或等于账单金额与手续费合计");
            }
        }
        if (Objects.equals(command.getBillType(), BillTypeEnum.TRANSFER.getValue())) {
            BigDecimal fromFee = sumExtras(command, 1, 1);
            BigDecimal fromDiscount = sumExtras(command, 2, 1);
            if (billModel.getAmount().add(fromFee).subtract(fromDiscount).compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(ErrorCode.Business.UPLOAD_IMPORT_EXCEL_FAILED, "转出账户侧优惠金额不能大于或等于转账金额与手续费合计");
            }
            BigDecimal toFee = sumExtras(command, 1, 2);
            BigDecimal toDiscount = sumExtras(command, 2, 2);
            if (billModel.getConvertedAmount().subtract(toFee).add(toDiscount).compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApiException(ErrorCode.Business.UPLOAD_IMPORT_EXCEL_FAILED, "转入账户侧手续费不能大于或等于转入金额与优惠合计");
            }
        }
    }

    private BigDecimal sumExtras(FortuneBillAddCommand command, Integer extraType, Integer accountSide) {
        return command.getExtras().stream()
                .filter(extra -> Objects.equals(extra.getExtraType(), extraType))
                .filter(extra -> Objects.isNull(accountSide) || Objects.equals(extra.getAccountSide(), accountSide))
                .map(FortuneBillExtraAddCommand::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
