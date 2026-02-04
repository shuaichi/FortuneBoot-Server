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
                ${ew.customSqlSegment}
            ) AS t
            """)
    BillStatisticsVo getBillStatistics(@Param(Constants.WRAPPER) Wrapper<FortuneBillEntity> wrapper);

    @Select("""
            <script>
                SELECT
                <choose>
                    <!-- 1-周：按天分组 -->
                    <when test="billTrendsQuery.timeGranularity == 1">DATE(trade_time)</when>
                    <!-- 2-月：年-月格式 -->
                    <when test="billTrendsQuery.timeGranularity == 2">DATE(trade_time)</when>
                    <!-- 3-年中的月份 -->
                    <when test="billTrendsQuery.timeGranularity == 3">DATE_FORMAT(trade_time, '%Y-%m')</when>
                    <!-- 4-年：年份 -->
                    <when test="billTrendsQuery.timeGranularity == 4">YEAR(trade_time)</when>
                </choose>
                AS `name`,
                SUM(amount) AS `value`
                FROM fortune_bill
                WHERE deleted = 0 
                    AND book_id = #{billTrendsQuery.bookId}
                    AND include = TRUE
                    AND bill_type = #{billTrendsQuery.billType}
                <choose>
                    <!-- 周维度：匹配该周 -->
                    <when test="billTrendsQuery.timeGranularity == 1">
                        AND trade_time >= DATE_SUB(#{billTrendsQuery.timePoint}, INTERVAL 6 DAY)
                        AND trade_time &lt; DATE_ADD(#{billTrendsQuery.timePoint}, INTERVAL 1 DAY)
                    </when>
                    <!-- 月维度：匹配年月 -->
                    <when test="billTrendsQuery.timeGranularity == 2">
                        AND trade_time >= DATE_SUB(#{billTrendsQuery.timePoint}, INTERVAL 30 DAY)
                        AND trade_time &lt; DATE_ADD(#{billTrendsQuery.timePoint}, INTERVAL 1 DAY)
                    </when>
                    <!-- 年维度：匹配该年份 -->
                    <when test="billTrendsQuery.timeGranularity == 3">
                        AND trade_time >= DATE_SUB(#{billTrendsQuery.timePoint}, INTERVAL 12 MONTH)
                        AND trade_time &lt; DATE_ADD(#{billTrendsQuery.timePoint}, INTERVAL 1 DAY)
                    </when>
                    <!-- 4-年维度：无时间条件 -->
                </choose>
                GROUP BY `name`
                ORDER BY `name` ASC
            </script>
            """)
    List<FortuneLineVo> getBillTrends(@Param("billTrendsQuery") BillTrendsQuery billTrendsQuery);

    @Select(
            """
                    <script>
                        WITH filtered_bills AS (
                          SELECT b.bill_id, b.amount
                          FROM fortune_bill b
                          <where>
                            b.include = TRUE
                            AND b.bill_type = #{billType}
                            AND b.deleted = 0
                            <if test='query.bookId != null'> AND b.book_id = #{query.bookId}</if>
                            <if test='query.title != null'> AND b.title LIKE CONCAT('%', #{query.title}, '%')</if>
                            <if test='query.startDate != null'> AND b.trade_time &gt;= #{query.startDate}</if>
                            <if test='query.endDate != null'> AND b.trade_time &lt;= CONCAT(#{query.endDate}, ' 23:59:59')</if>
                            <if test='query.accountIds != null and query.accountIds.size() > 0'>
                              AND b.account_id IN
                              <foreach collection='query.accountIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>
                            </if>
                            <if test='query.payeeIds != null and query.payeeIds.size() > 0'>
                              AND b.payee_id IN
                              <foreach collection='query.payeeIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>
                            </if>
                            <if test='query.categoryIds != null and query.categoryIds.size() > 0'>
                              AND EXISTS (SELECT 1 FROM fortune_category_relation cr
                               WHERE cr.bill_id = b.bill_id AND cr.category_id IN
                               <foreach collection='query.categoryIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>)
                            </if>
                            <if test='query.tagIds != null and query.tagIds.size() > 0'>
                              AND EXISTS (SELECT 1 FROM fortune_tag_relation tr
                               WHERE tr.bill_id = b.bill_id AND tr.tag_id IN
                               <foreach collection='query.tagIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>)
                            </if>
                          </where>
                        ),
                        total AS (SELECT SUM(amount) AS total_amount FROM filtered_bills)
                        SELECT c.category_name AS name
                               , SUM(fb.amount) AS `value`,
                               (SUM(fb.amount) / (SELECT total_amount FROM total)) * 100 AS percent
                        FROM filtered_bills fb
                        INNER JOIN fortune_category_relation cr ON fb.bill_id = cr.bill_id
                        INNER JOIN fortune_category c ON cr.category_id = c.category_id
                        CROSS JOIN total t
                        <if test='query.categoryIds != null and query.categoryIds.size() > 0'>
                          WHERE cr.category_id IN
                          <foreach collection='query.categoryIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>
                        </if>
                        GROUP BY c.category_id
                    </script>
                    """
    )
    List<FortunePieVo> getCategoryInclude(@Param("billType") Integer billType, @Param("query") CategoryIncludeQuery query);

    @Select(
            """
                    <script>
                        SELECT
                          COALESCE(ft.tag_name, '未设置标签') AS name,
                          SUM(b.amount) AS `value`
                        FROM fortune_bill b
                        LEFT JOIN fortune_tag_relation btr ON b.bill_id = btr.bill_id
                        LEFT JOIN fortune_tag ft ON btr.tag_id = ft.tag_id
                        WHERE b.include = TRUE
                            AND b.bill_type = #{billType}
                            AND b.deleted = 0
                          <if test='query.bookId != null'> AND b.book_id = #{query.bookId} </if>
                          <if test='query.title != null'> AND b.title LIKE CONCAT('%', #{query.title}, '%') </if>
                          <if test='query.startDate != null'> AND b.trade_time &gt;= #{query.startDate} </if>
                          <if test='query.endDate != null'> AND b.trade_time &lt;= CONCAT(#{query.endDate}, ' 23:59:59') </if>
                          <if test='query.accountIds != null and query.accountIds.size() > 0'>
                            AND b.account_id IN
                            <foreach collection='query.accountIds' item='item' open='(' separator=',' close=')'>
                              #{item}
                            </foreach>
                          </if>
                          <if test='query.payeeIds != null and query.payeeIds.size() > 0'>
                            AND b.payee_id IN
                            <foreach collection='query.payeeIds' item='item' open='(' separator=',' close=')'>
                              #{item}
                            </foreach>
                          </if>
                          <if test='query.categoryIds != null and query.categoryIds.size() > 0'>
                            AND EXISTS (SELECT 1 FROM fortune_category_relation cr
                              WHERE cr.bill_id = b.bill_id
                              AND cr.category_id IN
                              <foreach collection='query.categoryIds' item='item' open='(' separator=',' close=')'>
                                #{item}
                              </foreach>)
                          </if>
                          <if test='query.tagIds != null and query.tagIds.size() > 0'>
                            AND EXISTS (SELECT 1 FROM fortune_tag_relation tr
                              WHERE tr.bill_id = b.bill_id
                              AND tr.tag_id IN
                              <foreach collection='query.tagIds' item='item' open='(' separator=',' close=')'>
                                #{item}
                              </foreach>)
                          </if>
                        GROUP BY ft.tag_id
                    </script>
                    """
    )
    List<FortuneBarVo> getTagInclude(@Param("billType") Integer billType, @Param("query") TagIncludeQuery query);

    @Select(
            """
                    <script>
                        SELECT
                            COALESCE(p.payee_name, '未设置交易对象') AS name,
                            SUM(b.amount) AS value,
                            (SUM(b.amount) / SUM(SUM(b.amount)) OVER ()) * 100 AS `percent`
                        FROM fortune_bill b
                        LEFT JOIN fortune_payee p ON b.payee_id = p.payee_id
                        WHERE b.include = TRUE
                            AND b.bill_type = #{billType}
                            AND b.deleted = 0
                        <if test='query.accountIds != null and !query.accountIds.isEmpty()'>
                            AND b.account_id IN
                            <foreach item='item' collection='query.accountIds' open='(' separator=',' close=')'>
                                #{item}
                            </foreach>
                        </if>
                        <if test='query.payeeIds != null and !query.payeeIds.isEmpty()'>
                            AND b.payee_id IN
                            <foreach item='item' collection='query.payeeIds' open='(' separator=',' close=')'>
                                #{item}
                            </foreach>
                        </if>
                        <if test='query.bookId != null'>
                            AND b.book_id = #{query.bookId}
                        </if>
                        <if test='query.title != null and query.title != ""'>
                            AND b.title LIKE CONCAT('%', #{query.title}, '%')
                        </if>
                        <if test='query.startDate != null'>
                            AND b.trade_time &gt;= #{query.startDate}
                        </if>
                        <if test='query.endDate != null'>
                            AND b.trade_time &lt;= CONCAT(#{query.endDate}, ' 23:59:59')
                        </if>
                        <if test='query.categoryIds != null and !query.categoryIds.isEmpty()'>
                            AND EXISTS (
                                SELECT 1
                                FROM fortune_category_relation cr
                                WHERE cr.bill_id = b.bill_id
                                AND cr.category_id IN
                                <foreach item='item' collection='query.categoryIds' open='(' separator=',' close=')'>
                                    #{item}
                                </foreach>
                            )
                        </if>
                        <if test='query.tagIds != null and !query.tagIds.isEmpty()'>
                            AND EXISTS (
                                SELECT 1
                                FROM fortune_tag_relation tr
                                WHERE tr.bill_id = b.bill_id
                                AND tr.tag_id IN
                                <foreach item='item' collection='query.tagIds' open='(' separator=',' close=')'>
                                    #{item}
                                </foreach>
                            )
                        </if>
                        GROUP BY b.payee_id
                    </script>
                    """
    )
    List<FortunePieVo> getPayeeInclude(@Param("billType") Integer billType, @Param("query") PayeeIncludeQuery query);
}
