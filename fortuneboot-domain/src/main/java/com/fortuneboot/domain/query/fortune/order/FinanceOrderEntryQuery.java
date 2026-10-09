package com.fortuneboot.domain.query.fortune.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 单据明细分页查询
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderEntryQuery {

    public static final int DEFAULT_PAGE_NUM = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 账本id
     */
    @NotNull(message = "账本id不能为空")
    @Positive(message = "账本id只能是正数")
    private Long bookId;

    /**
     * 单据id
     */
    @NotNull(message = "单据id不能为空")
    @Positive(message = "单据id只能是正数")
    private Long orderId;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = MAX_PAGE_SIZE, message = "每页数量不能超过100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 流水状态，默认有效
     */
    private EntryStatus entryStatus = EntryStatus.ACTIVE;

    /**
     * 流水状态类型
     */
    public enum EntryStatus {
        ACTIVE, VOIDED, ALL
    }
}
