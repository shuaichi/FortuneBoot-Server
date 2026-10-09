package com.fortuneboot.service.fortune.order;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 单据金额领域规则。
 * <p>统一单笔金额上限、精度和字符串化格式，禁止各服务复制校验常量。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public final class FinanceOrderAmountRule {

    /**
     * 单笔金额上限，同时受页面输入与数据库DECIMAL精度约束。
     */
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

    private static final int MAX_SCALE = 2;

    private FinanceOrderAmountRule() {
    }

    public static boolean isValid(BigDecimal amount) {
        return amount != null
                && amount.compareTo(BigDecimal.ZERO) > 0
                && amount.scale() <= MAX_SCALE
                && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    public static String text(BigDecimal amount) {
        return amount == null ? "null" : amount.toPlainString();
    }

    public static BigDecimal parse(String amount) {
        return amount == null ? BigDecimal.ZERO : new BigDecimal(amount);
    }

    public static String format(BigDecimal amount) {
        return amount == null ? null : amount.setScale(MAX_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
