package com.fortuneboot.domain.vo.fortune.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 单据详情VO（V2金额均为十进制字符串）
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderDetailVo {

    /**
     * 单据id
     */
    private Long orderId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 标题
     */
    private String title;

    /**
     * 单据类型
     */
    private Integer type;

    /**
     * 单据类型描述
     */
    private String typeDesc;

    /**
     * 单据状态
     */
    private Integer status;

    /**
     * 单据状态描述
     */
    private String statusDesc;

    /**
     * 交易对象id
     */
    private Long counterpartyId;

    /**
     * 交易对象名称
     */
    private String counterpartyName;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 到期日
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    /**
     * 单据提交时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 单据关闭时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 乐观锁版本号
     */
    private Long version;

    /**
     * 核对状态
     */
    private String reconciliationStatus;

    /**
     * 核对问题摘要
     */
    private String reconciliationNote;

    /**
     * 出金额
     */
    private String outAmount;

    /**
     * 入金额
     */
    private String inAmount;

    /**
     * 形成金额
     */
    private String issuedAmount;

    /**
     * 结算金额
     */
    private String settledAmount;

    /**
     * 差额结清累计金额
     */
    private String adjustedAmount;

    /**
     * 待结清金额
     */
    private String pendingAmount;

    /**
     * 利息收入
     */
    private String interestIncome;

    /**
     * 利息支出
     */
    private String interestExpense;

    /**
     * 手续费
     */
    private String feeAmount;

    /**
     * 净成本或净收入
     */
    private String netCostOrIncome;

    /**
     * 结算状态
     */
    private String settlementState;

    /**
     * 待确认流水数
     */
    private int pendingEntryCount;

    /**
     * 是否逾期
     */
    private boolean overdue;

    /**
     * 是否允许编辑
     */
    private boolean canEdit;

    /**
     * 是否允许删除
     */
    private boolean canDelete;

    /**
     * 是否允许归档
     */
    private boolean canClose;

    /**
     * 是否允许重开
     */
    private boolean canReopen;

    /**
     * 允许的流水动作
     */
    private String[] allowedEntryActions;

    /**
     * 只读原因
     */
    private String readOnlyReason;
}
