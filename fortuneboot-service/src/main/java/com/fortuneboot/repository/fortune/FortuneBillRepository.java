package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.vo.fortune.include.*;

import java.util.List;

/**
 * 账单流水Repository
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 23:29
 */
public interface FortuneBillRepository extends IService<FortuneBillEntity> {
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
     * @param bookId
     * @return
     */
    BillStatisticsVo getBillStatistics(Long bookId);

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
     * 统计支出分类情况
     *
     * @param query
     * @return
     */
    List<FortunePieVo> getCategoryExpense(CategoryIncludeQuery query);

    /**
     * 统计收入分类情况
     *
     * @param query
     * @return
     */
    List<FortunePieVo> getCategoryIncome(CategoryIncludeQuery query);
}
