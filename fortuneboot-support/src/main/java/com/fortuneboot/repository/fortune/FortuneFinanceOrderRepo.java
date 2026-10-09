package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderOptionQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper.FinanceOrderSummaryRow;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
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

    /**
     * 查询账本全部单据历史，包含逻辑删除记录。
     */
    List<FortuneFinanceOrderEntity> listByBookIdIncludeDeleted(Long bookId);

    /**
     * 判断账本是否存在任意单据历史（含逻辑删除）。
     */
    boolean existAnyByBookIdsIncludeDeleted(Collection<Long> bookIds);

    /**
     * 判断单个账本是否存在任意单据历史（含逻辑删除）。
     */
    boolean existAnyByBookIdIncludeDeleted(Long bookId);

    /**
     * 判断交易对象是否被任意单据历史引用（含逻辑删除）。
     */
    boolean existAnyByCounterpartyIdIncludeDeleted(Long counterpartyId);

    /**
     * 单据分页查询：账本隔离，支持类型、状态和关键字过滤，并按统一规则稳定排序。
     *
     * @param query 分页查询条件
     * @return 排序后的单据列表
     */
    List<FortuneFinanceOrderEntity> listByPageQuery(FinanceOrderPageQuery query);

    /**
     * 单据选择器查询：由流水类型映射单据类型，只返回已核对且可关联的单据。
     *
     * @param query 选择器查询条件
     * @return 排序后的单据列表
     */
    List<FortuneFinanceOrderEntity> listByOptionQuery(FinanceOrderOptionQuery query);

    /**
     * 更新单据元数据，允许清空到期日和备注。
     */
    boolean updateMetadata(Long orderId, String title, Long counterpartyId, String counterpartyName,
                           LocalDate dueDate, String remark);

    /**
     * 仅更新单据状态。
     */
    boolean updateStatus(Long orderId, FinanceOrderStatusEnum status);

    /**
     * 更新单据状态和归档时间，允许清空归档时间。
     */
    boolean updateStatusAndCloseTime(Long orderId, FinanceOrderStatusEnum status, LocalDateTime closeTime);
}
