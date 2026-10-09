package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderSummaryRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 单据表（报销单、借出单、借入单）
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/8/16 18:06
 */
public interface FortuneFinanceOrderMapper extends BaseMapper<FortuneFinanceOrderEntity> {

    @Select("""
            select
              coalesce(sum(case when t.type = #{loanOutType} and t.pending_amount > 0 then t.pending_amount else 0 end), 0) as receivableAmount,
              coalesce(sum(case when t.type = #{loanInType} and t.pending_amount > 0 then t.pending_amount else 0 end), 0) as payableAmount,
              coalesce(sum(case when t.type = #{expenseClaimType} and t.pending_amount > 0 then t.pending_amount else 0 end), 0) as reimbursementAmount,
              coalesce(sum(case when t.type = #{loanOutType} and t.pending_amount > 0 and t.due_date is not null and t.due_date < #{today} then t.pending_amount else 0 end), 0) as overdueReceivableAmount,
              coalesce(sum(case when t.type = #{loanInType} and t.pending_amount > 0 and t.due_date is not null and t.due_date < #{today} then t.pending_amount else 0 end), 0) as overduePayableAmount,
              coalesce(sum(case when t.type = #{expenseClaimType} and t.pending_amount > 0 and t.due_date is not null and t.due_date < #{today} then t.pending_amount else 0 end), 0) as overdueReimbursementAmount,
              count(case when t.type = #{loanOutType} and t.pending_amount > 0 then 1 end) as receivableCount,
              count(case when t.type = #{loanInType} and t.pending_amount > 0 then 1 end) as payableCount,
              count(case when t.type = #{expenseClaimType} and t.pending_amount > 0 then 1 end) as reimbursementCount
            from (
              select type, due_date,
                (case when type = #{loanInType} then in_amount else out_amount end)
                - (case when type = #{loanInType} then out_amount else in_amount end)
                - adjusted_amount as pending_amount
              from fortune_finance_order
              where book_id = #{bookId} and deleted = 0 and reconciliation_status = #{readyStatus}
            ) t
            """)
    FinanceOrderSummaryRow summarizeReadyOrders(@Param("bookId") Long bookId,
                                                @Param("today") LocalDate today,
                                                @Param("expenseClaimType") Integer expenseClaimType,
                                                @Param("loanOutType") Integer loanOutType,
                                                @Param("loanInType") Integer loanInType,
                                                @Param("readyStatus") String readyStatus);

    @Select("""
            select count(*)
            from fortune_bill b
            inner join fortune_finance_order o on o.order_id = b.order_id
            where b.book_id = #{bookId} and b.deleted = 0 and b.recycle_bin = 0 and b.confirm = 0
              and o.deleted = 0 and o.reconciliation_status = #{readyStatus}
            """)
    int countPendingReadyEntries(@Param("bookId") Long bookId, @Param("readyStatus") String readyStatus);

    @Select("""
            <script>
            select count(*) from fortune_bill b
            where b.book_id = #{bookId} and b.deleted = 0 and b.order_id is not null
              and b.bill_type in
              <foreach collection="billTypes" item="billType" open="(" separator="," close=")">
                  #{billType}
              </foreach>
              and not exists (select 1 from fortune_finance_order o where o.order_id = b.order_id and o.deleted = 0)
            </script>
            """)
    int countOrphanBills(@Param("bookId") Long bookId, @Param("billTypes") Collection<Integer> billTypes);

    @Update("""
            UPDATE fortune_finance_order
            SET version = version + 1
            WHERE order_id = #{orderId}
              AND book_id = #{bookId}
              AND version = #{expectedVersion}
              AND deleted = 0
            """)
    int compareAndIncrementVersion(@Param("orderId") Long orderId,
                                   @Param("bookId") Long bookId,
                                   @Param("expectedVersion") Long expectedVersion);

    @Select("select * from fortune_finance_order where book_id = #{bookId} order by order_id")
    List<FortuneFinanceOrderEntity> listByBookIdIncludeDeleted(@Param("bookId") Long bookId);

    @Select("""
            <script>
            select count(*) from fortune_finance_order
            where book_id in
            <foreach collection="bookIds" item="bookId" open="(" separator="," close=")">
                #{bookId}
            </foreach>
            </script>
            """)
    int countByBookIdsIncludeDeleted(@Param("bookIds") Collection<Long> bookIds);

    @Select("select count(*) from fortune_finance_order where book_id = #{bookId}")
    int countByBookIdIncludeDeleted(@Param("bookId") Long bookId);

    @Select("select count(*) from fortune_finance_order where counterparty_id = #{counterpartyId}")
    int countByCounterpartyIdIncludeDeleted(@Param("counterpartyId") Long counterpartyId);

}
