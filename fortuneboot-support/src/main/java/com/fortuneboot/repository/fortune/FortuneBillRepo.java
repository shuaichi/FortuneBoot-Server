package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.common.enums.fortune.CategoryTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.include.*;

import java.util.Collection;
import java.util.List;

/**
 * 账单流水Repository
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 23:29
 */
public interface FortuneBillRepo extends IService<FortuneBillEntity> {
    /**
     * 分页查询
     *
     * @param page
     * @param wrapper
     * @return
     */
    IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, LambdaQueryWrapper<FortuneBillEntity> wrapper);


    /**
     * 根据账本id查询账单
     *
     * @param bookId
     * @return
     */
    List<FortuneBillEntity> getByBookId(Long bookId);

    /**
     * 根据账本idList查询账单
     *
     * @param bookIds
     * @return
     */
    List<FortuneBillEntity> getByBookIds(List<Long> bookIds);

    /**
     * 根据交易对象id判断是否已被使用
     *
     * @param payeeId
     * @return
     */
    Boolean existByPayeeId(Long payeeId);

    /**
     * 根据账户查询是否存在
     *
     * @param accountId
     * @return
     */
    Boolean existByAccount(Long accountId);

    /**
     * 统计收入支出
     *
     * @param query
     * @return
     */
    BillStatisticsVo getBillStatistics(FortuneBillQuery query);

    /**
     * 统计收入
     *
     * @param billTrendsQuery
     * @return
     */
    List<FortuneLineVo> getExpenseTrends(BillTrendsQuery billTrendsQuery);

    /**
     * 统计收入趋势
     *
     * @param billTrendsQuery
     * @return
     */
    List<FortuneLineVo> getIncomeTrends(BillTrendsQuery billTrendsQuery);

    /**
     * 统计分类情况
     *
     * @param query
     * @return
     */
    List<FortunePieVo> getCategoryInclude(CategoryTypeEnum typeEnum, CategoryIncludeQuery query);

    /**
     * 统计标签情况
     *
     * @param typeEnum
     * @param query
     * @return
     */
    List<FortuneBarVo> getTagInclude(CategoryTypeEnum typeEnum, TagIncludeQuery query);

    /**
     * 统计交易对象情况
     *
     * @param categoryTypeEnum
     * @param query
     * @return
     */
    List<FortunePieVo> getPayeeInclude(CategoryTypeEnum categoryTypeEnum, PayeeIncludeQuery query);

    /**
     * 根据orderId查询是否存在
     *
     * @param orderId
     * @return
     */
    Boolean existByOrderId(Long orderId);

    /**
     * 查询账本全部关联历史流水，包含逻辑删除与作废记录。
     */
    List<FortuneBillEntity> listByBookIdIncludeDeleted(Long bookId);

    /**
     * 判断账户是否被任意历史流水引用（含逻辑删除与作废）。
     */
    boolean existAnyAccountReferenceIncludeDeleted(Long accountId);

    /**
     * 判断交易对象是否被任意历史流水引用（含逻辑删除与作废）。
     */
    boolean existAnyPayeeReferenceIncludeDeleted(Long payeeId);

    /**
     * 校验单据是否有关联流水；包含已作废和逻辑删除的历史流水，绕过默认逻辑删除过滤。
     *
     * @param orderId 单据id
     * @return 是否存在任何关联流水
     */
    Boolean existAnyByOrderIdIncludeDeleted(Long orderId);

    /**
     * 批量查询存在任何关联流水（含已作废/逻辑删除）的单据ID。
     *
     * @param orderIds 单据id集合
     * @return 存在历史流水的单据ID
     */
    List<Long> listOrderIdsWithAnyBillIncludeDeleted(List<Long> orderIds);

    /**
     * 根据单据id查询
     *
     * @param orderId
     * @return
     */
    List<FortuneBillEntity> getByOrderId(Long orderId);

    /**
     * 单据明细分页查询：按单据隔离并按回收站状态过滤，交易时间倒序。
     *
     * @param query 明细分页查询条件
     * @return 明细分页结果
     */
    Page<FortuneBillEntity> pageByEntryQuery(FinanceOrderEntryQuery query);

    /**
     * 按单据ID批量查询有效流水。
     *
     * @param orderIds 单据id集合
     * @return 关联流水
     */
    List<FortuneBillEntity> listByOrderIds(Collection<Long> orderIds);

    List<FortuneLineVo> getDateInclude(BillIncludeQuery query);

    List<FortuneBarVo> getMemberInclude(BillIncludeQuery query);

    List<BillCompareVo> getBillCompare(BillCompareQuery query);

    List<IncomeExpenseCalendarItemVo> getIncomeExpenseCalendar(IncomeExpenseCalendarQuery query);

    List<FortuneBarVo> getBillRank(BillRankQuery query);

    List<HeatmapVo> getCalendarHeatmap(CalendarHeatmapQuery query);

    List<AccountIncludeVo> getAccountInclude(BillIncludeQuery query);

    List<BillTypeDistributionVo> getBillTypeDistribution(BillIncludeQuery query);

    java.math.BigDecimal getMaxSingleExpense(BillIncludeQuery query);

    Integer countUnconfirmedBills(BillIncludeQuery query);
}
