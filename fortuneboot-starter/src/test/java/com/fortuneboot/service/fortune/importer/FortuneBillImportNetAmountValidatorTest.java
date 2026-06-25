package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class FortuneBillImportNetAmountValidatorTest {

    private final FortuneBillImportNetAmountValidator validator = new FortuneBillImportNetAmountValidator();

    @Test
    @DisplayName("支出优惠大于净额时失败")
    void validate_expenseNegativeNet_throws() {
        FortuneBillAddCommand command = command(BillTypeEnum.EXPENSE.getValue(), extra(2, 1, "12"));
        FortuneBillModel bill = bill("10", "10");

        assertThatThrownBy(() -> validator.validate(command, bill)).isInstanceOf(ApiException.class)
                .hasMessageContaining("优惠金额不能大于或等于账单金额与手续费合计");
    }

    @Test
    @DisplayName("转账转入侧手续费大于换算后金额时失败")
    void validate_transferNegativeToNet_throws() {
        FortuneBillAddCommand command = command(BillTypeEnum.TRANSFER.getValue(), extra(1, 2, "12"));
        FortuneBillModel bill = bill("100", "10");

        assertThatThrownBy(() -> validator.validate(command, bill)).isInstanceOf(ApiException.class)
                .hasMessageContaining("转入账户侧手续费不能大于或等于转入金额与优惠合计");
    }

    @Test
    @DisplayName("净额为正时通过")
    void validate_positiveNet_passes() {
        FortuneBillAddCommand command = command(BillTypeEnum.EXPENSE.getValue(), extra(1, 1, "1"));
        FortuneBillModel bill = bill("10", "10");

        assertThatCode(() -> validator.validate(command, bill)).doesNotThrowAnyException();
    }

    private FortuneBillAddCommand command(Integer billType, FortuneBillExtraAddCommand extra) {
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setBillType(billType);
        command.setExtras(List.of(extra));
        return command;
    }

    private FortuneBillExtraAddCommand extra(Integer type, Integer side, String amount) {
        FortuneBillExtraAddCommand extra = new FortuneBillExtraAddCommand();
        extra.setExtraType(type);
        extra.setAccountSide(side);
        extra.setAmount(new BigDecimal(amount));
        return extra;
    }

    private FortuneBillModel bill(String amount, String convertedAmount) {
        FortuneBillModel bill = new FortuneBillModel(mock(FortuneBillRepo.class));
        bill.setAmount(new BigDecimal(amount));
        bill.setConvertedAmount(new BigDecimal(convertedAmount));
        return bill;
    }
}
