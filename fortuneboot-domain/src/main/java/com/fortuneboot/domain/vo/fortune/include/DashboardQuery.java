package com.fortuneboot.domain.vo.fortune.include;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * 首页聚合看板查询
 *
 * @author zhangchi118
 * @date 2026/8/4 19:34
 **/
@Data
public class DashboardQuery {

    /**
     * 账本id
     */
    @NotNull(message = "账本不能为空")
    @Positive(message = "账本ID必须是正数")
    private Long bookId;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 周期类型：1-本月，2-本年，3-自定义
     */
    private Integer periodType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}
