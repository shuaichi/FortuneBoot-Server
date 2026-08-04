package com.fortuneboot.domain.vo.fortune.include;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首页聚合看板
 *
 * @author zhangchi118
 * @date 2026/8/4 19:35
 **/
@Data
public class DashboardVo {

    private BillStatisticsVo period;

    private BillStatisticsVo previous;

    private BigDecimal ringIncomeRate;

    private BigDecimal ringExpenseRate;

    private BigDecimal totalAssets;

    private BigDecimal totalLiabilities;

    private BigDecimal netAssets;

    private BigDecimal avgDailyExpense;

    private BigDecimal maxSingleExpense;

    private Integer unconfirmedCount;

    private Integer pendingReceivable;

    private List<FortuneLineVo> recentTrend;
}
