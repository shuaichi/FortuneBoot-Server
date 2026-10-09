package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
/**
 * 单据流水现金效果计算器。
 */
@Component
public class FinanceOrderCashEffectCalculator {

    public Map<Long, BigDecimal> calculate(FortuneBillEntity bill) {
        if (bill == null || !FinanceOrderLedgerVersionEnum.isCurrentValue(bill.getOrderLedgerVersion())
                || !Boolean.TRUE.equals(bill.getConfirm())
                || Boolean.TRUE.equals(bill.getDeleted())
                || Boolean.TRUE.equals(bill.getRecycleBin())) {
            return Map.of();
        }
        FinanceOrderComponentEnum component = FinanceOrderComponentEnum.getByValue(bill.getOrderComponent());
        if (component == null) {
            return Map.of();
        }
        BigDecimal amount = validateAmount(bill.getAmount());
        return switch (component) {
            case PRINCIPAL -> principalEffect(bill, amount);
            case INTEREST -> interestEffect(bill, amount);
            case FEE -> feeEffect(bill, amount);
            case WRITE_OFF -> Map.of();
        };
    }

    private Map<Long, BigDecimal> principalEffect(FortuneBillEntity bill, BigDecimal amount) {
        Long accountId = bill.getAccountId();
        if (accountId == null) {
            return Map.of();
        }
        if (BillTypeEnum.isPrincipalOutValue(bill.getBillType())) {
            return Map.of(accountId, amount.negate());
        }
        if (BillTypeEnum.isPrincipalInValue(bill.getBillType())) {
            return Map.of(accountId, amount);
        }
        return Map.of();
    }

    private Map<Long, BigDecimal> interestEffect(FortuneBillEntity bill, BigDecimal amount) {
        Long accountId = bill.getAccountId();
        if (accountId == null) {
            return Map.of();
        }
        if (BillTypeEnum.isInterestIncomeValue(bill.getBillType())) {
            return Map.of(accountId, amount);
        }
        if (BillTypeEnum.isInterestExpenseValue(bill.getBillType())) {
            return Map.of(accountId, amount.negate());
        }
        return Map.of();
    }

    private Map<Long, BigDecimal> feeEffect(FortuneBillEntity bill, BigDecimal amount) {
        Long accountId = bill.getAccountId();
        if (accountId == null || !BillTypeEnum.isInterestExpenseValue(bill.getBillType())) {
            return Map.of();
        }
        return Map.of(accountId, amount.negate());
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (!FinanceOrderAmountRule.isValid(amount)) {
            throw new ApiException(ErrorCode.Business.ORDER_AMOUNT_INVALID, FinanceOrderAmountRule.text(amount));
        }
        return amount;
    }
}
