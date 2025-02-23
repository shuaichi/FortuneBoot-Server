package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.vo.fortune.bill.BillStatisticsVo;
import com.fortuneboot.domain.vo.fortune.include.FortuneLineVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单流水Mapper
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:27
 **/
public interface FortuneBillMapper extends BaseMapper<FortuneBillEntity> {

    @Select("SELECT\n" +
            "    bill.*\n" +
            "FROM fortune_bill AS bill\n" +
            "LEFT JOIN fortune_category_relation AS fcr ON bill.bill_id = fcr.bill_id\n" +
            "LEFT JOIN fortune_tag_relation AS ftr ON bill.bill_id = ftr.bill_id\n" +
            " ${ew.customSqlSegment}\n" +
            "GROUP BY bill.bill_id\n" +
            "ORDER BY bill.trade_time DESC, bill.create_time DESC")
    IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, @Param(Constants.WRAPPER) Wrapper<FortuneBillEntity> wrapper);

    @Select("SELECT \n" +
            "    SUM(CASE WHEN bill_type = 1 THEN amount ELSE 0 END) AS income,\n" +
            "    SUM(CASE WHEN bill_type = 2 THEN amount ELSE 0 END) AS expense,\n" +
            "    SUM(CASE WHEN bill_type = 1 THEN amount ELSE -amount END) AS surplus\n" +
            "FROM fortune_bill\n" +
            "WHERE book_id = ${bookId}\n" +
            "   AND include = TRUE\n" +
            "   AND deleted = 0;")
    BillStatisticsVo getBillStatistics(Long bookId);
    
    @Select("SELECT\n" +
            "  CASE \n" +
            "    WHEN :${TimeGranularity} = 1 THEN DATE(trade_time)  -- 天维度\n" +
            "    WHEN :${TimeGranularity} = 2 THEN DATE_FORMAT(trade_time, '%Y-%m')  -- 月维度\n" +
            "    WHEN :${TimeGranularity} = 3 THEN DATE_FORMAT(trade_time, '%Y-%m')  -- 年下按月\n" +
            "    WHEN :${TimeGranularity} = 4 THEN DATE_FORMAT(trade_time, '%Y')     -- 年维度\n" +
            "  END AS `name`,\n" +
            "  SUM(amount) AS `value`\n" +
            "FROM fortune_bill\n" +
            "WHERE\n" +
            "  CASE \n" +
            "    WHEN :${TimeGranularity} = 1 THEN  -- 指定周\n" +
            "      trade_time BETWEEN \n" +
            "        STR_TO_DATE(:${TimePoint}, '%Y-%m-%d') - INTERVAL (WEEKDAY(:${TimePoint})) DAY\n" +
            "        AND \n" +
            "        STR_TO_DATE(:${TimePoint}, '%Y-%m-%d') + INTERVAL (6 - WEEKDAY(:${TimePoint})) DAY\n" +
            "        \n" +
            "    WHEN :${TimeGranularity} = 2 THEN  -- 指定月\n" +
            "      DATE_FORMAT(trade_time, '%Y-%m') = DATE_FORMAT(:${TimePoint}, '%Y-%m')\n" +
            "      \n" +
            "    WHEN :${TimeGranularity} = 3 THEN  -- 指定年\n" +
            "      YEAR(trade_time) = YEAR(:${TimePoint})\n" +
            "      \n" +
            "    WHEN :${TimeGranularity} = 4 THEN  -- 全部时间\n" +
            "      1=1\n" +
            "  END\n" +
            "  AND book_id=${bookId}\n" +
            "  AND deleted=0\n" +
            "GROUP BY time_gran\n" +
            "ORDER BY time_gran;\n")
    List<FortuneLineVo> getExpenseTrends(Long bookId, Integer TimeGranularity, LocalDateTime TimePoint);

    List<FortuneLineVo> getIncomeTrends(Long bookId);
}
