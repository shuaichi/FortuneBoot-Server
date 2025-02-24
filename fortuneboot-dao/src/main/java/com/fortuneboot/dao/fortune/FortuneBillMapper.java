package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.vo.fortune.include.BillStatisticsVo;
import com.fortuneboot.domain.vo.fortune.include.FortuneLineVo;
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

    @Select("""
            SELECT\n
                bill.*\n
            FROM fortune_bill AS bill\n
            LEFT JOIN fortune_category_relation AS fcr ON bill.bill_id = fcr.bill_id\n
            LEFT JOIN fortune_tag_relation AS ftr ON bill.bill_id = ftr.bill_id\n
             ${ew.customSqlSegment}\n
            GROUP BY bill.bill_id\n
            ORDER BY bill.trade_time DESC, bill.create_time DESC
            """)
    IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, @Param(Constants.WRAPPER) Wrapper<FortuneBillEntity> wrapper);

    @Select("""
            SELECT \n
                SUM(CASE WHEN bill_type = 1 THEN amount ELSE 0 END) AS income,\n
                SUM(CASE WHEN bill_type = 2 THEN amount ELSE 0 END) AS expense,\n
                SUM(CASE WHEN bill_type = 1 THEN amount ELSE -amount END) AS surplus\n
            FROM fortune_bill\n
            WHERE book_id = ${bookId}\n
               AND include = TRUE\n
               AND deleted = 0
            """)
    BillStatisticsVo getBillStatistics(Long bookId);

    @Select("""
                <script>
                        SELECT
                        <choose>
                            <!-- 1-周：按天分组 -->
                            <when test="timeGranularity == 1">DATE(trade_time)</when>
                            <!-- 2-月：年-月格式 -->
                            <when test="timeGranularity == 2">DATE_FORMAT(trade_time, '%Y-%m')</when>
                            <!-- 3-年中的月份 -->
                            <when test="timeGranularity == 3">DATE_FORMAT(trade_time, '%Y-%m')</when>
                            <!-- 4-年：年份 -->
                            <when test="timeGranularity == 4">YEAR(trade_time)</when>
                        </choose>\s
                        AS `name`,
                        SUM(amount) AS `value`
                        FROM fortune_bill
                        WHERE deleted = 0 
                            AND book_id = #{bookId}
                            AND include = TRUE
                            AND bill_type = #{billType}
                        <choose>
                            <!-- 周维度：匹配该周 -->
                            <when test="timeGranularity == 1">
                                AND YEARWEEK(trade_time, 1) = YEARWEEK(#{timePoint}, 1)
                            </when>
                            <!-- 月维度：匹配年月 -->
                            <when test="timeGranularity == 2">
                                AND DATE_FORMAT(trade_time, '%Y-%m') = DATE_FORMAT(#{timePoint}, '%Y-%m')
                            </when>
                            <!-- 年维度：匹配该年份 -->
                            <when test="timeGranularity == 3">
                                AND trade_time >= DATE_FORMAT(#{timePoint}, '%Y-01-01')
                                AND trade_time &lt; DATE_ADD(DATE_FORMAT(#{timePoint}, '%Y-01-01'), INTERVAL 1 YEAR)
                            </when>
                            <!-- 4-年维度：无时间条件 -->
                        </choose>
                        GROUP BY `name`
                        ORDER BY `name` ASC
                    </script>
            """)
    List<FortuneLineVo> getBillTrends(@Param("bookId") Long bookId,
                                      @Param("timeGranularity") Integer timeGranularity,
                                      @Param("timePoint") LocalDateTime timePoint,
                                      @Param("billType") Integer billType);
}
