package com.fortuneboot.service.fortune;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 * 账单附加费用分类校验器。
 *
 * @author fortuneboot
 */
@Component
public class FortuneBillExtraCategoryValidator {

    public void validate(FortuneBillAddCommand command) {
        if (command == null || isTransfer(command) || isEmpty(command.getExtras())) {
            return;
        }
        boolean hasMissingCategory = command.getExtras().stream()
                .anyMatch(extra -> extra == null || extra.getCategoryId() == null);
        if (hasMissingCategory) {
            throw new ApiException(ErrorCode.Business.BILL_EXTRA_CATEGORY_REQUIRED);
        }
    }

    private boolean isTransfer(FortuneBillAddCommand command) {
        return BillTypeEnum.TRANSFER.getValue().equals(command.getBillType());
    }

    private boolean isEmpty(Collection<FortuneBillExtraAddCommand> extras) {
        return extras == null || extras.isEmpty();
    }
}
