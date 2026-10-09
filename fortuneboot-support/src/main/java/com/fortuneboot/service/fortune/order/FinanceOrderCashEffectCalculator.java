package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 单据流水现金效果计算器。
 */
@Component
public class FinanceOrderCashEffectCalculator {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

    private static final Set<Integer> PRINCIPAL_OUT_TYPES = Set.of(7, 9, 12);
    private static final Set<Integer> PRINCIPAL_IN_TYPES = Set.of(8, 10, 11);

    public Map<Long, BigDecimal> calculate(FortuneBillEntity bill) {
        if (bill == null || !Integer.valueOf(1).equals(bill.getOrderLedgerVersion())
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
        if (PRINCIPAL_OUT_TYPES.contains(bill.getBillType())) {
            return Map.of(accountId, amount.negate());
        }
        if (PRINCIPAL_IN_TYPES.contains(bill.getBillType())) {
            return Map.of(accountId, amount);
        }
        return Map.of();
    }

    private Map<Long, BigDecimal> interestEffect(FortuneBillEntity bill, BigDecimal amount) {
        Long accountId = bill.getAccountId();
        if (accountId == null) {
            return Map.of();
        }
        if (Objects.equals(bill.getBillType(), 2)) {
            return Map.of(accountId, amount);
        }
        if (Objects.equals(bill.getBillType(), 1)) {
            return Map.of(accountId, amount.negate());
        }
        return Map.of();
    }

    private Map<Long, BigDecimal> feeEffect(FortuneBillEntity bill, BigDecimal amount) {
        Long accountId = bill.getAccountId();
        if (accountId == null || !Objects.equals(bill.getBillType(), 1)) {
            return Map.of();
        }
        return Map.of(accountId, amount.negate());
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ApiException(ErrorCode.Business.ORDER_AMOUNT_INVALID,
                    amount == null ? "null" : amount.toPlainString());
        }
        return amount;
    }
}
