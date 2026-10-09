package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper.FinanceOrderSummaryRow;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;

import java.time.LocalDate;
import java.util.List;

/**
 * 单据表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:11
 **/
public interface FortuneFinanceOrderRepo extends IService<FortuneFinanceOrderEntity> {

    /**
     * 根据账本ID查询单据
     *
     * @param bookId
     * @return
     */
    List<FortuneFinanceOrderEntity> getUsingFinanceOrderList(Long bookId);

    /**
     * 理财收益统计
     *
     * @param bookId 账本ID
     * @param query 查询条件
     * @return 理财收益
     */
    List<FinanceProfitVo> getFinanceProfit(Long bookId, FinanceProfitQuery query);

    int compareAndIncrementVersion(Long orderId, Long bookId, Long expectedVersion);

    FinanceOrderSummaryRow summarizeReadyOrders(Long bookId, LocalDate today);

    int countPendingReadyEntries(Long bookId);

    int countReviewRequired(Long bookId);

    int countOrphanBills(Long bookId);
}
