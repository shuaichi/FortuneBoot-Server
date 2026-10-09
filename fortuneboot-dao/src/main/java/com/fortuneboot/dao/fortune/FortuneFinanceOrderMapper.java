package com.fortuneboot.dao.fortune;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
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
              coalesce(sum(case when t.type = 2 and t.pending_amount > 0 then t.pending_amount else 0 end), 0) as receivableAmount,
              coalesce(sum(case when t.type = 3 and t.pending_amount > 0 then t.pending_amount else 0 end), 0) as payableAmount,
              coalesce(sum(case when t.type = 1 and t.pending_amount > 0 then t.pending_amount else 0 end), 0) as reimbursementAmount,
              coalesce(sum(case when t.type = 2 and t.pending_amount > 0 and t.due_date is not null and t.due_date < #{today} then t.pending_amount else 0 end), 0) as overdueReceivableAmount,
              coalesce(sum(case when t.type = 3 and t.pending_amount > 0 and t.due_date is not null and t.due_date < #{today} then t.pending_amount else 0 end), 0) as overduePayableAmount,
              coalesce(sum(case when t.type = 1 and t.pending_amount > 0 and t.due_date is not null and t.due_date < #{today} then t.pending_amount else 0 end), 0) as overdueReimbursementAmount,
              count(case when t.type = 2 and t.pending_amount > 0 then 1 end) as receivableCount,
              count(case when t.type = 3 and t.pending_amount > 0 then 1 end) as payableCount,
              count(case when t.type = 1 and t.pending_amount > 0 then 1 end) as reimbursementCount
            from (
              select type, due_date,
                (case when type = 3 then in_amount else out_amount end)
                - (case when type = 3 then out_amount else in_amount end)
                - adjusted_amount as pending_amount
              from fortune_finance_order
              where book_id = #{bookId} and deleted = 0 and reconciliation_status = 'READY'
            ) t
            """)
    FinanceOrderSummaryRow summarizeReadyOrders(@Param("bookId") Long bookId, @Param("today") LocalDate today);

    @Select("""
            select count(*)
            from fortune_bill b
            inner join fortune_finance_order o on o.order_id = b.order_id
            where b.book_id = #{bookId} and b.deleted = 0 and b.recycle_bin = 0 and b.confirm = 0
              and o.deleted = 0 and o.reconciliation_status = 'READY'
            """)
    int countPendingReadyEntries(@Param("bookId") Long bookId);

    @Select("""
            select count(*) from fortune_finance_order
            where book_id = #{bookId} and deleted = 0 and reconciliation_status = 'REVIEW_REQUIRED'
            """)
    int countReviewRequired(@Param("bookId") Long bookId);

    @Select("""
            select count(*) from fortune_bill b
            where b.book_id = #{bookId} and b.deleted = 0 and b.order_id is not null
              and not exists (select 1 from fortune_finance_order o where o.order_id = b.order_id and o.deleted = 0)
            """)
    int countOrphanBills(@Param("bookId") Long bookId);

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

    List<FinanceProfitVo> getFinanceProfit(@Param("bookId") Long bookId, @Param("query") FinanceProfitQuery query);

    /**
     * 单据看板数据库聚合行（内部使用）
     */
    class FinanceOrderSummaryRow {
        private BigDecimal receivableAmount = BigDecimal.ZERO;
        private BigDecimal payableAmount = BigDecimal.ZERO;
        private BigDecimal reimbursementAmount = BigDecimal.ZERO;
        private BigDecimal overdueReceivableAmount = BigDecimal.ZERO;
        private BigDecimal overduePayableAmount = BigDecimal.ZERO;
        private BigDecimal overdueReimbursementAmount = BigDecimal.ZERO;
        private long receivableCount;
        private long payableCount;
        private long reimbursementCount;

        public BigDecimal getReceivableAmount() {
            return receivableAmount;
        }

        public void setReceivableAmount(BigDecimal receivableAmount) {
            this.receivableAmount = receivableAmount;
        }

        public BigDecimal getPayableAmount() {
            return payableAmount;
        }

        public void setPayableAmount(BigDecimal payableAmount) {
            this.payableAmount = payableAmount;
        }

        public BigDecimal getReimbursementAmount() {
            return reimbursementAmount;
        }

        public void setReimbursementAmount(BigDecimal reimbursementAmount) {
            this.reimbursementAmount = reimbursementAmount;
        }

        public BigDecimal getOverdueReceivableAmount() {
            return overdueReceivableAmount;
        }

        public void setOverdueReceivableAmount(BigDecimal overdueReceivableAmount) {
            this.overdueReceivableAmount = overdueReceivableAmount;
        }

        public BigDecimal getOverduePayableAmount() {
            return overduePayableAmount;
        }

        public void setOverduePayableAmount(BigDecimal overduePayableAmount) {
            this.overduePayableAmount = overduePayableAmount;
        }

        public BigDecimal getOverdueReimbursementAmount() {
            return overdueReimbursementAmount;
        }

        public void setOverdueReimbursementAmount(BigDecimal overdueReimbursementAmount) {
            this.overdueReimbursementAmount = overdueReimbursementAmount;
        }

        public long getReceivableCount() {
            return receivableCount;
        }

        public void setReceivableCount(long receivableCount) {
            this.receivableCount = receivableCount;
        }

        public long getPayableCount() {
            return payableCount;
        }

        public void setPayableCount(long payableCount) {
            this.payableCount = payableCount;
        }

        public long getReimbursementCount() {
            return reimbursementCount;
        }

        public void setReimbursementCount(long reimbursementCount) {
            this.reimbursementCount = reimbursementCount;
        }
    }
}
