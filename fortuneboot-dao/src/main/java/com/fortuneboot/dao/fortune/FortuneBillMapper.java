package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.vo.fortune.include.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 账单流水Mapper
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:27
 **/
public interface FortuneBillMapper extends BaseMapper<FortuneBillEntity> {

    @Select("""
            SELECT\n
                bill.*\n
            FROM fortune_bill AS bill\n
            LEFT JOIN fortune_category_relation AS fcr ON bill.bill_id = fcr.bill_id\n
            LEFT JOIN fortune_tag_relation AS ftr ON bill.bill_id = ftr.bill_id\n
            LEFT JOIN fortune_member_relation AS fmr ON bill.bill_id = fmr.bill_id AND fmr.deleted = 0\n
             ${ew.customSqlSegment}
            """)
    IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, @Param(Constants.WRAPPER) Wrapper<FortuneBillEntity> wrapper);

    @Select("""
            
            SELECT\s
                SUM(t.expense) AS expense,
                SUM(t.income) AS income,
                SUM(t.income) - SUM(t.expense) AS surplus
            FROM (
                SELECT\s
                    bill.bill_id,
                    MAX(CASE WHEN bill.bill_type = 1 THEN bill.amount ELSE 0 END) AS expense,
                    MAX(CASE WHEN bill.bill_type = 2 THEN bill.amount ELSE 0 END) AS income
                FROM fortune_bill AS bill
                LEFT JOIN fortune_category_relation AS fcr ON bill.bill_id = fcr.bill_id
                LEFT JOIN fortune_tag_relation AS ftr ON bill.bill_id = ftr.bill_id
                LEFT JOIN fortune_member_relation AS fmr ON bill.bill_id = fmr.bill_id AND fmr.deleted = 0
                ${ew.customSqlSegment}
            ) AS t
            """)
    BillStatisticsVo getBillStatistics(@Param(Constants.WRAPPER) Wrapper<FortuneBillEntity> wrapper);

    /**
     * 账单趋势查询 - 使用 XML 中的 databaseId 分发
     */
    List<FortuneLineVo> getBillTrends(@Param("billTrendsQuery") BillTrendsQuery billTrendsQuery);

    /**
     * 分类统计 - 使用 XML 中的 databaseId 分发
     */
    List<FortunePieVo> getCategoryInclude(@Param("billType") Integer billType, @Param("query") CategoryIncludeQuery query);

    /**
     * 标签统计 - 使用 XML 中的 databaseId 分发
     */
    List<FortuneBarVo> getTagInclude(@Param("billType") Integer billType, @Param("query") TagIncludeQuery query);

    /**
     * 交易对象统计 - 使用 XML 中的 databaseId 分发
     */
    List<FortunePieVo> getPayeeInclude(@Param("billType") Integer billType, @Param("query") PayeeIncludeQuery query);

    /**
     * 统计日期支出
     */
    List<FortuneLineVo> getDateInclude(@Param("query") BillIncludeQuery query);

    /**
     * 成员维度统计
     */
    List<FortuneBarVo> getMemberInclude(@Param("query") BillIncludeQuery query);

    /**
     * 收支对比
     */
    List<BillCompareVo> getBillCompare(@Param("query") BillCompareQuery query);

    /**
     * 收支日历
     */
    List<IncomeExpenseCalendarItemVo> getIncomeExpenseCalendar(@Param("query") IncomeExpenseCalendarQuery query);

    /**
     * 收支排行
     */
    List<FortuneBarVo> getBillRank(@Param("query") BillRankQuery query);

    /**
     * 日历热力图
     */
    List<HeatmapVo> getCalendarHeatmap(@Param("query") CalendarHeatmapQuery query);

    /**
     * 账户维度统计
     */
    List<AccountIncludeVo> getAccountInclude(@Param("query") BillIncludeQuery query);

    /**
     * 账单类型分布
     */
    List<BillTypeDistributionVo> getBillTypeDistribution(@Param("query") BillIncludeQuery query);

    /**
     * 借贷待收明细
     */
    List<LoanDetailVo> getLoanReceivableDetails(@Param("bookId") Long bookId);

    /**
     * 借贷待还明细
     */
    List<LoanDetailVo> getLoanPayableDetails(@Param("bookId") Long bookId);

    /**
     * 最大单笔支出
     */
    java.math.BigDecimal getMaxSingleExpense(@Param("query") BillIncludeQuery query);

    /**
     * 未确认账单数量
     */
    Integer countUnconfirmedBills(@Param("query") BillIncludeQuery query);
}
