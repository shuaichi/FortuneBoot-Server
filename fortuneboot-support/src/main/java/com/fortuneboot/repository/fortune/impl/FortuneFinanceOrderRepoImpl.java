package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 单据表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:12
 **/
@Service
@AllArgsConstructor
public class FortuneFinanceOrderRepoImpl
        extends ServiceImpl<FortuneFinanceOrderMapper, FortuneFinanceOrderEntity>
        implements FortuneFinanceOrderRepo {

    @Override
    public List<FortuneFinanceOrderEntity> getUsingFinanceOrderList(Long bookId) {
        LambdaQueryWrapper<FortuneFinanceOrderEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        queryWrapper.eq(FortuneFinanceOrderEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderEntity::getStatus, FinanceOrderStatusEnum.USING.getValue());
        return this.list(queryWrapper);
    }

    @Override
    public List<FinanceProfitVo> getFinanceProfit(Long bookId, FinanceProfitQuery query) {
        return baseMapper.getFinanceProfit(bookId, query);
    }

    @Override
    public int compareAndIncrementVersion(Long orderId, Long bookId, Long expectedVersion) {
        return baseMapper.compareAndIncrementVersion(orderId, bookId, expectedVersion);
    }

    @Override
    public com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper.FinanceOrderSummaryRow summarizeReadyOrders(
            Long bookId, LocalDate today) {
        return baseMapper.summarizeReadyOrders(bookId, today);
    }

    @Override
    public int countPendingReadyEntries(Long bookId) {
        return baseMapper.countPendingReadyEntries(bookId);
    }

    @Override
    public int countReviewRequired(Long bookId) {
        return baseMapper.countReviewRequired(bookId);
    }

    @Override
    public int countOrphanBills(Long bookId) {
        return baseMapper.countOrphanBills(bookId);
    }

    @Override
    public List<FortuneFinanceOrderEntity> listByBookIdIncludeDeleted(Long bookId) {
        return baseMapper.listByBookIdIncludeDeleted(bookId);
    }

    @Override
    public boolean existAnyByBookIdsIncludeDeleted(Collection<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return false;
        }
        return baseMapper.countByBookIdsIncludeDeleted(bookIds) > 0;
    }

    @Override
    public boolean existAnyByBookIdIncludeDeleted(Long bookId) {
        return baseMapper.countByBookIdIncludeDeleted(bookId) > 0;
    }

    @Override
    public boolean existAnyByCounterpartyIdIncludeDeleted(Long counterpartyId) {
        return baseMapper.countByCounterpartyIdIncludeDeleted(counterpartyId) > 0;
    }
}
