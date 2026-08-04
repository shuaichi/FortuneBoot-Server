package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 账单统计通用查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:33
 **/
@Data
public class BillIncludeQuery {

    /**
     * 账本id
     */
    @NotNull(message = "账本不能为空")
    @Positive(message = "账本ID必须是正数")
    private Long bookId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 标题
     */
    private String title;

    /**
     * 账单类型
     */
    private Integer billType;

    /**
     * 分类
     */
    private List<Long> categoryIds;

    /**
     * 标签
     */
    private List<Long> tagIds;

    /**
     * 交易对象
     */
    private List<Long> payeeIds;

    /**
     * 账户
     */
    private List<Long> accountIds;

    /**
     * 成员
     */
    private List<Long> memberIds;

    /**
     * 是否确认
     */
    private Boolean confirm;

    /**
     * 是否统计
     */
    private Boolean include;
}
