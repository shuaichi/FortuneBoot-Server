package com.fortuneboot.common.enums.fortune;

import com.fortuneboot.common.enums.BasicEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 20:19
 **/
@Getter
public enum FinanceOrderTypeEnum implements BasicEnum<Integer> {
    EXPENSE_CLAIM(1, "费用报销单"),
    LOAN_OUT(2, "债权单"),
    LOAN_IN(3, "债务单"),
    ;

    private final Integer value;

    private final String description;

    FinanceOrderTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static String getDescByValue(Integer type) {
        for (FinanceOrderTypeEnum financeOrderTypeEnum : values()) {
            if (Objects.equals(financeOrderTypeEnum.getValue(), type)) {
                return financeOrderTypeEnum.getDescription();
            }
        }
        return StringUtils.EMPTY;
    }

    public static FinanceOrderTypeEnum getByValue(Integer value) {
        for (FinanceOrderTypeEnum orderType : values()) {
            if (Objects.equals(orderType.getValue(), value)) {
                return orderType;
            }
        }
        return null;
    }

    /**
     * 流水动作到单据类型的显式领域映射，替代按数字区间推断。
     */
    public static FinanceOrderTypeEnum fromBillType(BillTypeEnum billType) {
        if (billType == null) {
            return null;
        }
        return switch (billType) {
            case ADVANCE, REIMBURSE -> EXPENSE_CLAIM;
            case LOAN_OUT, LOAN_RECOVER -> LOAN_OUT;
            case LOAN_IN, LOAN_REPAY -> LOAN_IN;
            default -> null;
        };
    }

    /**
     * 单据类型 + 组件 + 流水类型白名单，集中维护历史分散的硬编码集合。
     */
    public boolean allows(FinanceOrderComponentEnum component, BillTypeEnum billType) {
        if (component == null || billType == null) {
            return false;
        }
        return switch (this) {
            case EXPENSE_CLAIM -> switch (component) {
                case PRINCIPAL -> billType == BillTypeEnum.ADVANCE || billType == BillTypeEnum.REIMBURSE;
                case FEE, WRITE_OFF -> billType == BillTypeEnum.EXPENSE;
                default -> false;
            };
            case LOAN_OUT -> switch (component) {
                case PRINCIPAL -> billType == BillTypeEnum.LOAN_OUT || billType == BillTypeEnum.LOAN_RECOVER;
                case INTEREST -> billType == BillTypeEnum.INCOME;
                case FEE, WRITE_OFF -> billType == BillTypeEnum.EXPENSE;
                default -> false;
            };
            case LOAN_IN -> switch (component) {
                case PRINCIPAL -> billType == BillTypeEnum.LOAN_IN || billType == BillTypeEnum.LOAN_REPAY;
                case INTEREST, FEE -> billType == BillTypeEnum.EXPENSE;
                case WRITE_OFF -> billType == BillTypeEnum.INCOME;
            };
        };
    }

    /**
     * 草稿状态下允许的单据入口动作。
     */
    public List<FinanceOrderEntryActionEnum> initialEntryActions() {
        return switch (this) {
            case EXPENSE_CLAIM -> List.of(FinanceOrderEntryActionEnum.ADVANCE);
            case LOAN_OUT -> List.of(FinanceOrderEntryActionEnum.LOAN_OUT);
            case LOAN_IN -> List.of(FinanceOrderEntryActionEnum.LOAN_IN);
        };
    }

    /**
     * 进行中状态下允许的单据入口动作，顺序即接口展示顺序。
     */
    public List<FinanceOrderEntryActionEnum> usingEntryActions() {
        return switch (this) {
            case EXPENSE_CLAIM -> List.of(FinanceOrderEntryActionEnum.ADVANCE,
                    FinanceOrderEntryActionEnum.REIMBURSE, FinanceOrderEntryActionEnum.FEE,
                    FinanceOrderEntryActionEnum.WRITE_OFF);
            case LOAN_OUT -> List.of(FinanceOrderEntryActionEnum.LOAN_OUT,
                    FinanceOrderEntryActionEnum.LOAN_RECOVER, FinanceOrderEntryActionEnum.INTEREST_IN,
                    FinanceOrderEntryActionEnum.FEE, FinanceOrderEntryActionEnum.WRITE_OFF);
            case LOAN_IN -> List.of(FinanceOrderEntryActionEnum.LOAN_IN,
                    FinanceOrderEntryActionEnum.LOAN_REPAY, FinanceOrderEntryActionEnum.INTEREST_OUT,
                    FinanceOrderEntryActionEnum.FEE, FinanceOrderEntryActionEnum.WRITE_OFF);
        };
    }
}
