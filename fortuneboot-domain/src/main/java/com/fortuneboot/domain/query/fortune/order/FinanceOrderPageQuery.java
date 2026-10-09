package com.fortuneboot.domain.query.fortune.order;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 单据分页查询
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Data
public class FinanceOrderPageQuery {

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
     * 单据类型
     *
     * @see com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum
     */
    private Integer type;

    /**
     * 单据状态
     *
     * @see com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum
     */
    private Integer status;

    /**
     * 结算状态
     *
     * @see com.fortuneboot.common.enums.fortune.FinanceOrderSettlementStateEnum
     */
    private String settlementState;

    /**
     * 关键字，同时匹配标题和交易对象名称
     */
    private String keyword;

    /**
     * 是否只看逾期
     */
    private Boolean overdueOnly = false;

    public void setKeyword(String keyword) {
        this.keyword = StringUtils.trimToNull(keyword);
    }
}
