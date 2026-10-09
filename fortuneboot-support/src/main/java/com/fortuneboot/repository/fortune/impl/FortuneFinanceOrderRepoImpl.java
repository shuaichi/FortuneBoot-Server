package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderOptionQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    /**
     * 默认列表排序：到期日升序空值最后，更新时间倒序空值最后，ID倒序保证稳定。
     */
    private static final Comparator<FortuneFinanceOrderEntity> DEFAULT_ORDER = Comparator
            .comparing(FortuneFinanceOrderEntity::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(FortuneFinanceOrderEntity::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(FortuneFinanceOrderEntity::getOrderId, Comparator.reverseOrder());

    @Override
    public List<FortuneFinanceOrderEntity> getUsingFinanceOrderList(Long bookId) {
        LambdaQueryWrapper<FortuneFinanceOrderEntity> queryWrapper =
                WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
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
        LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderEntity::getReconciliationStatus,
                        FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue());
        return (int) this.count(wrapper);
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

    @Override
    public List<FortuneFinanceOrderEntity> listByPageQuery(FinanceOrderPageQuery query) {
        LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getBookId, query.getBookId())
                .eq(Objects.nonNull(query.getType()), FortuneFinanceOrderEntity::getType, query.getType())
                .eq(Objects.nonNull(query.getStatus()), FortuneFinanceOrderEntity::getStatus, query.getStatus());
        applyKeyword(wrapper, query.getKeyword());
        return sorted(this.list(wrapper));
    }

    @Override
    public List<FortuneFinanceOrderEntity> listByOptionQuery(FinanceOrderOptionQuery query) {
        BillTypeEnum billType = BillTypeEnum.getByValue(query.getBillType());
        FinanceOrderTypeEnum orderType = FinanceOrderTypeEnum.fromBillType(billType);
        if (billType == null || orderType == null) {
            return List.of();
        }
        LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getBookId, query.getBookId())
                .eq(FortuneFinanceOrderEntity::getType, orderType.getValue())
                .eq(FortuneFinanceOrderEntity::getReconciliationStatus,
                        FinanceOrderReconciliationStatusEnum.READY.getValue());
        if (billType.isOrderFormation()) {
            wrapper.in(FortuneFinanceOrderEntity::getStatus,
                    FinanceOrderStatusEnum.INIT.getValue(), FinanceOrderStatusEnum.USING.getValue());
        } else {
            wrapper.eq(FortuneFinanceOrderEntity::getStatus, FinanceOrderStatusEnum.USING.getValue());
        }
        applyKeyword(wrapper, query.getKeyword());
        return sorted(this.list(wrapper));
    }

    @Override
    public boolean updateMetadata(Long orderId, String title, Long counterpartyId, String counterpartyName,
                                  LocalDate dueDate, String remark) {
        LambdaUpdateWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaUpdateWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getOrderId, orderId)
                .set(FortuneFinanceOrderEntity::getTitle, title)
                .set(FortuneFinanceOrderEntity::getCounterpartyId, counterpartyId)
                .set(FortuneFinanceOrderEntity::getCounterpartyName, counterpartyName)
                .set(FortuneFinanceOrderEntity::getDueDate, dueDate)
                .set(FortuneFinanceOrderEntity::getRemark, remark);
        return this.update(wrapper);
    }

    @Override
    public boolean updateStatusAndCloseTime(Long orderId, FinanceOrderStatusEnum status, LocalDateTime closeTime) {
        LambdaUpdateWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaUpdateWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getOrderId, orderId)
                .set(FortuneFinanceOrderEntity::getStatus, status.getValue())
                .set(FortuneFinanceOrderEntity::getCloseTime, closeTime);
        return this.update(wrapper);
    }

    @Override
    public boolean updateStatus(Long orderId, FinanceOrderStatusEnum status) {
        LambdaUpdateWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaUpdateWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getOrderId, orderId)
                .set(FortuneFinanceOrderEntity::getStatus, status.getValue());
        return this.update(wrapper);
    }

    private void applyKeyword(LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper, String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(condition -> condition
                    .like(FortuneFinanceOrderEntity::getTitle, keyword)
                    .or()
                    .like(FortuneFinanceOrderEntity::getCounterpartyName, keyword));
        }
    }

    private List<FortuneFinanceOrderEntity> sorted(List<FortuneFinanceOrderEntity> orders) {
        orders.sort(DEFAULT_ORDER);
        return orders;
    }
}
