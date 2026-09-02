package com.fortuneboot.service.fortune;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FortuneBillExtraCategoryValidatorTest {

    private final FortuneBillExtraCategoryValidator validator = new FortuneBillExtraCategoryValidator();

    @Test
    @DisplayName("非转账账单的附加费用缺少分类时拒绝")
    void validate_nonTransferExtraWithoutCategory_throwsException() {
        FortuneBillAddCommand command = command(BillTypeEnum.EXPENSE.getValue(), null);

        assertThatThrownBy(() -> validator.validate(command))
                .isInstanceOf(ApiException.class)
                .hasMessage("非转账账单的附加费用分类不能为空");
    }

    @Test
    @DisplayName("转账账单的附加费用允许不关联分类")
    void validate_transferExtraWithoutCategory_doesNotThrow() {
        FortuneBillAddCommand command = command(BillTypeEnum.TRANSFER.getValue(), null);

        assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("非转账账单的所有附加费用都有分类时通过")
    void validate_nonTransferExtrasWithCategory_doesNotThrow() {
        FortuneBillAddCommand command = command(BillTypeEnum.INCOME.getValue(), 1L);

        assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("没有附加费用的非转账账单通过")
    void validate_nonTransferWithoutExtras_doesNotThrow() {
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setBillType(BillTypeEnum.EXPENSE.getValue());

        assertThatCode(() -> validator.validate(command)).doesNotThrowAnyException();
    }

    private FortuneBillAddCommand command(Integer billType, Long categoryId) {
        FortuneBillExtraAddCommand extra = new FortuneBillExtraAddCommand();
        extra.setCategoryId(categoryId);

        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setBillType(billType);
        command.setExtras(List.of(extra));
        return command;
    }
}
