package com.fortuneboot.domain.vo.fortune.order;

import lombok.Data;

/**
 * 历史单据核对报告（金额均为十进制字符串）
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderReconciliationVo {

    /**
     * 单据id，孤立流水报告为空
     */
    private Long orderId;

    /**
     * 流水id，单据级报告为空
     */
    private Long billId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 异常代码
     */
    private String[] issueCodes;

    /**
     * 单据版本号，用于人工确认乐观锁
     */
    private Long version;

    /**
     * 原单据状态
     */
    private Integer originalStatus;

    /**
     * 原出金额
     */
    private String originalOutAmount;

    /**
     * 原入金额
     */
    private String originalInAmount;

    /**
     * 候选形成金额，无法安全计算时为空
     */
    private String candidateIssuedAmount;

    /**
     * 候选结算金额，无法安全计算时为空
     */
    private String candidateSettledAmount;

    /**
     * 候选待结清金额，无法安全计算时为空
     */
    private String candidatePendingAmount;

    /**
     * 相关账户
     */
    private Long[] accountIds;

    /**
     * 相关币种
     */
    private String[] currencyCodes;

    /**
     * 历史现金效果是否可证明已知
     */
    private boolean cashEffectKnown;

    /**
     * 核对说明
     */
    private String note;
}
