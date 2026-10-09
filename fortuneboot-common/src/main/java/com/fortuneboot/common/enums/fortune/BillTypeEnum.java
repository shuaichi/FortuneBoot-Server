package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 账单流水类型枚举
 * 1、支出;2、收入;3、转账;4、余额调整;5、盈利;6、亏损
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:21
 **/
@Getter
public enum BillTypeEnum implements BasicEnum<Integer> {
    EXPENSE(1, "支出"),
    INCOME(2, "收入"),
    TRANSFER(3, "转账"),
    ADJUST(4, "余额调整"),
    PROFIT(5, "盈利"),
    LOSS(6, "亏损"),
    ADVANCE(7, "垫付"),
    REIMBURSE(8, "报销"),
    LOAN_OUT(9, "借出"),
    LOAN_RECOVER(10, "收回"),
    LOAN_IN(11, "借入"),
    LOAN_REPAY(12, "归还"),
    ;

    private final Integer value;

    private final String description;

    BillTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BillTypeEnum getByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 全部合法流水类型编码，由枚举常量推导，禁止在业务代码中手写编码集合。
     */
    public static Set<Integer> validValues() {
        return Arrays.stream(values())
                .map(BillTypeEnum::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isValidValue(Integer value) {
        return getByValue(value) != null;
    }

    /**
     * 单据形成动作：垫付、借出、借入。
     */
    public boolean isOrderFormation() {
        return this == ADVANCE || this == LOAN_OUT || this == LOAN_IN;
    }

    public static boolean isOrderFormationValue(Integer value) {
        return matches(value, BillTypeEnum::isOrderFormation);
    }

    /**
     * 单据结算动作：报销到账、收回本金、归还本金。
     */
    public boolean isOrderSettlement() {
        return this == REIMBURSE || this == LOAN_RECOVER || this == LOAN_REPAY;
    }

    public static boolean isOrderSettlementValue(Integer value) {
        return matches(value, BillTypeEnum::isOrderSettlement);
    }

    /**
     * 本金出向：垫付、借出、归还。
     */
    public boolean isPrincipalOut() {
        return this == ADVANCE || this == LOAN_OUT || this == LOAN_REPAY;
    }

    public static boolean isPrincipalOutValue(Integer value) {
        return matches(value, BillTypeEnum::isPrincipalOut);
    }

    /**
     * 本金入向：报销、收回、借入。
     */
    public boolean isPrincipalIn() {
        return this == REIMBURSE || this == LOAN_RECOVER || this == LOAN_IN;
    }

    public static boolean isPrincipalInValue(Integer value) {
        return matches(value, BillTypeEnum::isPrincipalIn);
    }

    /**
     * 费用报销单历史流水类型：垫付、报销。
     */
    public boolean isClaimType() {
        return this == ADVANCE || this == REIMBURSE;
    }

    public static boolean isClaimValue(Integer value) {
        return matches(value, BillTypeEnum::isClaimType);
    }

    /**
     * 借贷单流水类型：借出、收回、借入、归还。
     */
    public boolean isLoanType() {
        return this == LOAN_OUT || this == LOAN_RECOVER || this == LOAN_IN || this == LOAN_REPAY;
    }

    public static boolean isLoanValue(Integer value) {
        return matches(value, BillTypeEnum::isLoanType);
    }

    public static Set<Integer> loanTypeValues() {
        return Arrays.stream(values())
                .filter(BillTypeEnum::isLoanType)
                .map(BillTypeEnum::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 利息收入流水使用收入类型编码。
     */
    public boolean isInterestIncome() {
        return this == INCOME;
    }

    public static boolean isInterestIncomeValue(Integer value) {
        return matches(value, BillTypeEnum::isInterestIncome);
    }

    /**
     * 利息支出流水使用支出类型编码。
     */
    public boolean isInterestExpense() {
        return this == EXPENSE;
    }

    public static boolean isInterestExpenseValue(Integer value) {
        return matches(value, BillTypeEnum::isInterestExpense);
    }

    private static boolean matches(Integer value, Predicate<BillTypeEnum> predicate) {
        BillTypeEnum billType = getByValue(value);
        return billType != null && predicate.test(billType);
    }

    public static String getDescByValue(Integer value) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.value, value))
                .findFirst()
                .map(BillTypeEnum::getDescription)
                .orElse(StringUtils.EMPTY);
    }
}
