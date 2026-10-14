package com.fortuneboot.domain.rule.fortune.order;

import org.apache.commons.lang3.StringUtils;

/**
 * 单据字段领域规则。
 * <p>集中维护标题、备注和交易对象名称长度约束，Command校验与服务端校验共用同一规则。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public final class FinanceOrderFieldRule {

    public static final int TITLE_MAX_LENGTH = 50;
    public static final int REMARK_MAX_LENGTH = 512;
    public static final int COUNTERPARTY_NAME_MAX_LENGTH = 64;
    public static final String DEFAULT_COUNTERPARTY_NAME = "未填写";

    private FinanceOrderFieldRule() {
    }

    public static boolean isValidTitle(String title) {
        return StringUtils.isNotBlank(title) && title.length() <= TITLE_MAX_LENGTH;
    }

    public static boolean isValidRemark(String remark) {
        return remark != null && remark.length() <= REMARK_MAX_LENGTH;
    }

    public static boolean isValidCounterpartyName(String counterpartyName) {
        return StringUtils.isNotBlank(counterpartyName)
                && counterpartyName.length() <= COUNTERPARTY_NAME_MAX_LENGTH;
    }
}
