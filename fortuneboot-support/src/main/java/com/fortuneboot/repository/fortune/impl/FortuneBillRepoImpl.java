package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.common.enums.fortune.CategoryTypeEnum;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneBillMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageUtil;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import org.apache.commons.collections4.CollectionUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:30
 **/
@Service
@AllArgsConstructor
public class FortuneBillRepoImpl extends ServiceImpl<FortuneBillMapper, FortuneBillEntity> implements FortuneBillRepo {

    private final FortuneBillMapper fortuneBillMapper;

    @Override
    public IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, LambdaQueryWrapper<FortuneBillEntity> wrapper) {
        return fortuneBillMapper.getPage(page, wrapper);
    }

    @Override
    public List<FortuneBillEntity> getByBookId(Long bookId) {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.eq(FortuneBillEntity::getBookId, bookId);
        return this.list(queryWrapper);
    }

    @Override
    public List<FortuneBillEntity> getByBookIds(List<Long> bookIds) {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.in(FortuneBillEntity::getBookId, bookIds);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean existByPayeeId(Long payeeId) {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.eq(FortuneBillEntity::getPayeeId, payeeId);
        return this.exists(queryWrapper);
    }

    @Override
    public Boolean existByAccount(Long accountId) {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.eq(FortuneBillEntity::getAccountId, accountId)
                .or().eq(FortuneBillEntity::getToAccountId, accountId);
        return this.exists(queryWrapper);
    }

    @Override
    public BillStatisticsVo getBillStatistics(FortuneBillQuery query) {
        return fortuneBillMapper.getBillStatistics(query.addQueryCondition());
    }

    @Override
    public List<FortuneLineVo> getExpenseTrends(BillTrendsQuery billTrendsQuery) {
        return fortuneBillMapper.getBillTrends(billTrendsQuery);
    }

    @Override
    public List<FortuneLineVo> getIncomeTrends(BillTrendsQuery billTrendsQuery) {
        return fortuneBillMapper.getBillTrends(billTrendsQuery);
    }

    @Override
    public List<FortunePieVo> getCategoryInclude(CategoryTypeEnum typeEnum, CategoryIncludeQuery query) {
        return fortuneBillMapper.getCategoryInclude(typeEnum.getValue(), query);
    }

    @Override
    public List<FortuneBarVo> getTagInclude(CategoryTypeEnum typeEnum, TagIncludeQuery query) {
        return fortuneBillMapper.getTagInclude(typeEnum.getValue(), query);
    }

    @Override
    public List<FortunePieVo> getPayeeInclude(CategoryTypeEnum typeEnum, PayeeIncludeQuery query) {
        return fortuneBillMapper.getPayeeInclude(typeEnum.getValue(), query);
    }

    @Override
    public Boolean existByOrderId(Long orderId) {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.eq(FortuneBillEntity::getOrderId, orderId);
        return this.exists(queryWrapper);
    }

    @Override
    public List<FortuneBillEntity> listByBookIdIncludeDeleted(Long bookId) {
        return fortuneBillMapper.listByBookIdIncludeDeleted(bookId);
    }

    @Override
    public boolean existAnyAccountReferenceIncludeDeleted(Long accountId) {
        return fortuneBillMapper.countAccountReferencesIncludeDeleted(accountId) > 0;
    }

    @Override
    public boolean existAnyPayeeReferenceIncludeDeleted(Long payeeId) {
        return fortuneBillMapper.countPayeeReferencesIncludeDeleted(payeeId) > 0;
    }

    @Override
    public Boolean existAnyByOrderIdIncludeDeleted(Long orderId) {
        return fortuneBillMapper.countByOrderIdIncludeDeleted(orderId) > 0;
    }

    @Override
    public List<Long> listOrderIdsWithAnyBillIncludeDeleted(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return List.of();
        }
        return fortuneBillMapper.listOrderIdsWithAnyBillIncludeDeleted(orderIds);
    }

    @Override
    public List<FortuneBillEntity> getByOrderId(Long orderId) {
        LambdaQueryWrapper<FortuneBillEntity> queryWrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        queryWrapper.eq(FortuneBillEntity::getOrderId, orderId);
        return this.list(queryWrapper);
    }

    @Override
    public Page<FortuneBillEntity> pageByEntryQuery(FinanceOrderEntryQuery query) {
        LambdaQueryWrapper<FortuneBillEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        wrapper.eq(FortuneBillEntity::getOrderId, query.getOrderId());
        FinanceOrderEntryQuery.EntryStatus entryStatus = query.getEntryStatus();
        if (entryStatus == FinanceOrderEntryQuery.EntryStatus.ACTIVE) {
            wrapper.eq(FortuneBillEntity::getRecycleBin, false);
        } else if (entryStatus == FinanceOrderEntryQuery.EntryStatus.VOIDED) {
            wrapper.eq(FortuneBillEntity::getRecycleBin, true);
        }
        wrapper.orderByDesc(FortuneBillEntity::getTradeTime)
                .orderByDesc(FortuneBillEntity::getBillId);
        return this.page(new Page<>(FinanceOrderPageUtil.pageNum(query.getPageNum()),
                FinanceOrderPageUtil.pageSize(query.getPageSize())), wrapper);
    }

    @Override
    public List<FortuneBillEntity> listByOrderIds(Collection<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return List.of();
        }
        LambdaQueryWrapper<FortuneBillEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        wrapper.in(FortuneBillEntity::getOrderId, orderIds);
        return this.list(wrapper);
    }

    @Override
    public List<FortuneLineVo> getDateInclude(BillIncludeQuery query) {
        return fortuneBillMapper.getDateInclude(query);
    }

    @Override
    public List<FortuneBarVo> getMemberInclude(BillIncludeQuery query) {
        return fortuneBillMapper.getMemberInclude(query);
    }

    @Override
    public List<BillCompareVo> getBillCompare(BillCompareQuery query) {
        return fortuneBillMapper.getBillCompare(query);
    }

    @Override
    public List<IncomeExpenseCalendarItemVo> getIncomeExpenseCalendar(IncomeExpenseCalendarQuery query) {
        return fortuneBillMapper.getIncomeExpenseCalendar(query);
    }

    @Override
    public List<FortuneBarVo> getBillRank(BillRankQuery query) {
        return fortuneBillMapper.getBillRank(query);
    }

    @Override
    public List<HeatmapVo> getCalendarHeatmap(CalendarHeatmapQuery query) {
        return fortuneBillMapper.getCalendarHeatmap(query);
    }

    @Override
    public List<AccountIncludeVo> getAccountInclude(BillIncludeQuery query) {
        return fortuneBillMapper.getAccountInclude(query);
    }

    @Override
    public List<BillTypeDistributionVo> getBillTypeDistribution(BillIncludeQuery query) {
        return fortuneBillMapper.getBillTypeDistribution(query);
    }

    @Override
    public List<LoanDetailVo> getLoanReceivableDetails(Long bookId) {
        return fortuneBillMapper.getLoanReceivableDetails(bookId);
    }

    @Override
    public List<LoanDetailVo> getLoanPayableDetails(Long bookId) {
        return fortuneBillMapper.getLoanPayableDetails(bookId);
    }

    @Override
    public java.math.BigDecimal getMaxSingleExpense(BillIncludeQuery query) {
        return fortuneBillMapper.getMaxSingleExpense(query);
    }

    @Override
    public Integer countUnconfirmedBills(BillIncludeQuery query) {
        return fortuneBillMapper.countUnconfirmedBills(query);
    }
}
