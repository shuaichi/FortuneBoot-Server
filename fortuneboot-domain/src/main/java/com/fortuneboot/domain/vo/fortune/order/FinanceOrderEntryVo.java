package com.fortuneboot.domain.vo.fortune.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fortuneboot.domain.vo.fortune.FortuneMemberVo;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillExtraVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 单据关联流水VO（V2金额均为十进制字符串）
 * <p>
 * 复用FortuneBillVo的业务字段含义，但不改变旧FortuneBillVo的序列化契约。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderEntryVo {

    /**
     * 流水id
     */
    private Long billId;

    /**
     * 账本id
     */
    private Long bookId;

    /**
     * 账本名称
     */
    private String bookName;

    /**
     * 标题
     */
    private String title;

    /**
     * 交易时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeTime;

    /**
     * 账户id
     */
    private Long accountId;

    /**
     * 单据id
     */
    private Long orderId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 金额
     */
    private String amount;

    /**
     * 币种
     */
    private String currencyCode;

    /**
     * 被转入账户币种
     */
    private String toCurrencyCode;

    /**
     * 汇率转换后的金额
     */
    private String convertedAmount;

    /**
     * 交易对象id
     */
    private Long payeeId;

    /**
     * 交易对象名称
     */
    private String payeeName;

    /**
     * 流水类型
     */
    private Integer billType;

    /**
     * 转账到的账户id
     */
    private Long toAccountId;

    /**
     * 转账到的账户名称
     */
    private String toAccountName;

    /**
     * 是否确认
     */
    private Boolean confirm;

    /**
     * 是否统计
     */
    private Boolean include;

    /**
     * 备注
     */
    private String remark;

    /**
     * 分类金额
     */
    private List<FinanceOrderCategoryAmountVo> categoryAmountPair;

    /**
     * 标签
     */
    private List<FortuneTagVo> tagList;

    /**
     * 成员
     */
    private List<FortuneMemberVo> memberList;

    /**
     * 是否存在附件
     */
    private Boolean hasFile = false;

    /**
     * 附加费用
     */
    private List<FortuneBillExtraVo> extras;

    /**
     * 单据组件
     */
    private String orderComponent;

    /**
     * 单据账本版本
     */
    private Integer orderLedgerVersion;

    /**
     * 是否作废（回收站）
     */
    private Boolean recycleBin;

    /**
     * 允许的流水操作
     */
    private String[] allowedActions;

    /**
     * 只读原因
     */
    private String readOnlyReason;
}
