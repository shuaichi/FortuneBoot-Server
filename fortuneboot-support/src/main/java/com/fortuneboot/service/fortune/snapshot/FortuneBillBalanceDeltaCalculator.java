package com.fortuneboot.service.fortune.snapshot;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 账单余额变动计算器
 *
 * @author zhangchi118
 * @date 2026/8/4
 */
@Slf4j
@Component
public class FortuneBillBalanceDeltaCalculator {

    private static final int EXTRA_TYPE_FEE = 1;
    private static final int EXTRA_TYPE_DISCOUNT = 2;
    private static final int ACCOUNT_SIDE_FROM = 1;
    private static final int ACCOUNT_SIDE_TO = 2;

    public List<AccountBalanceChange> calculate(FortuneBillEntity bill, List<FortuneBillExtraEntity> extras) {
        if (bill == null || bill.getTradeTime() == null) {
            return Collections.emptyList();
        }
        BillTypeEnum billType = BillTypeEnum.getByValue(bill.getBillType());
        if (billType == null) {
            return Collections.emptyList();
        }
        LocalDate tradeDate = bill.getTradeTime().toLocalDate();
        return switch (billType) {
            case EXPENSE -> expenseChange(bill, extras, tradeDate);
            case INCOME -> accountChange(bill.getAccountId(), tradeDate, amount(bill.getAmount()));
            case TRANSFER -> transferChanges(bill, extras, tradeDate);
            case ADJUST -> adjustChange(bill, tradeDate);
            case PROFIT -> accountChange(bill.getAccountId(), tradeDate, amount(bill.getAmount()));
            case ADVANCE -> accountChange(bill.getAccountId(), tradeDate, amount(bill.getAmount()).negate());
            case REIMBURSE -> accountChange(bill.getAccountId(), tradeDate, amount(bill.getAmount()));
            case LOSS, LOAN_OUT, LOAN_RECOVER, LOAN_IN, LOAN_REPAY -> Collections.emptyList();
        };
    }

    private List<AccountBalanceChange> expenseChange(FortuneBillEntity bill, List<FortuneBillExtraEntity> extras, LocalDate tradeDate) {
        BigDecimal actualAmount = amount(bill.getAmount())
                .add(sumExtra(extras, EXTRA_TYPE_FEE, null))
                .subtract(sumExtra(extras, EXTRA_TYPE_DISCOUNT, null));
        return accountChange(bill.getAccountId(), tradeDate, actualAmount.negate());
    }

    private List<AccountBalanceChange> transferChanges(FortuneBillEntity bill, List<FortuneBillExtraEntity> extras, LocalDate tradeDate) {
        BigDecimal fromDeduct = amount(bill.getAmount())
                .add(sumExtra(extras, EXTRA_TYPE_FEE, ACCOUNT_SIDE_FROM))
                .subtract(sumExtra(extras, EXTRA_TYPE_DISCOUNT, ACCOUNT_SIDE_FROM));
        BigDecimal toCredit = amount(bill.getConvertedAmount())
                .subtract(sumExtra(extras, EXTRA_TYPE_FEE, ACCOUNT_SIDE_TO))
                .add(sumExtra(extras, EXTRA_TYPE_DISCOUNT, ACCOUNT_SIDE_TO));
        return List.of(
                new AccountBalanceChange(bill.getAccountId(), tradeDate, fromDeduct.negate()),
                new AccountBalanceChange(bill.getToAccountId(), tradeDate, toCredit)
        ).stream().filter(change -> change.accountId() != null).toList();
    }

    private List<AccountBalanceChange> adjustChange(FortuneBillEntity bill, LocalDate tradeDate) {
        BigDecimal amount = amount(bill.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            return accountChange(bill.getAccountId(), tradeDate, amount);
        }
        BigDecimal convertedAmount = amount(bill.getConvertedAmount());
        if (convertedAmount.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("余额调整账单 amount 为 0，使用 convertedAmount 修复快照反推，billId={}, accountId={}, tradeDate={}",
                    bill.getBillId(), bill.getAccountId(), tradeDate);
            return accountChange(bill.getAccountId(), tradeDate, convertedAmount);
        }
        log.warn("余额调整账单 amount 和 convertedAmount 均为 0，跳过快照余额变动，billId={}, accountId={}, tradeDate={}",
                bill.getBillId(), bill.getAccountId(), tradeDate);
        return Collections.emptyList();
    }

    private List<AccountBalanceChange> accountChange(Long accountId, LocalDate tradeDate, BigDecimal amount) {
        if (accountId == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyList();
        }
        return List.of(new AccountBalanceChange(accountId, tradeDate, amount));
    }

    private BigDecimal sumExtra(List<FortuneBillExtraEntity> extras, Integer extraType, Integer accountSide) {
        if (extras == null || extras.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return extras.stream()
                .filter(Objects::nonNull)
                .filter(extra -> Objects.equals(extraType, extra.getExtraType()))
                .filter(extra -> accountSide == null || Objects.equals(accountSide, extra.getAccountSide()))
                .map(FortuneBillExtraEntity::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal amount(BigDecimal amount) {
        return Objects.requireNonNullElse(amount, BigDecimal.ZERO);
    }
}
