package com.fortuneboot.domain.vo.fortune.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 单据看板汇总VO（V2金额均为十进制字符串）
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderSummaryVo {

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 待收回金额（债权单）
     */
    private String receivableAmount;

    /**
     * 待归还金额（债务单）
     */
    private String payableAmount;

    /**
     * 待报销金额（费用报销单）
     */
    private String reimbursementAmount;

    /**
     * 逾期待收回金额
     */
    private String overdueReceivableAmount;

    /**
     * 逾期待归还金额
     */
    private String overduePayableAmount;

    /**
     * 逾期待报销金额
     */
    private String overdueReimbursementAmount;

    /**
     * 待收回单据数
     */
    private int receivableCount;

    /**
     * 待归还单据数
     */
    private int payableCount;

    /**
     * 待报销单据数
     */
    private int reimbursementCount;

    /**
     * 待确认流水数
     */
    private int pendingEntryCount;

    /**
     * 待核对单据数
     */
    private int reviewRequiredCount;

    /**
     * 孤立历史流水数
     */
    private int orphanLegacyBillCount;

    /**
     * 统计日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate asOfDate;
}
