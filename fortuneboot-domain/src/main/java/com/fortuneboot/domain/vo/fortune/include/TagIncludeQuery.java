package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 标签统计查询
 *
 * @author zhangchi118
 * @date 2025/3/2 16:07
 **/
@Data
public class TagIncludeQuery {

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
}
