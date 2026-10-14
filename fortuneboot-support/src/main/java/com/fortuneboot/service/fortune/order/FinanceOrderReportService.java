package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.rule.fortune.order.FinanceOrderFieldRule;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;
import com.fortuneboot.domain.vo.fortune.include.LoanDetailVo;
import com.fortuneboot.domain.vo.fortune.include.LoanOverviewVo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 单据统计报表服务。
 * <p>承接借贷总览与理财收益统计；查询条件由Repository封装，本服务只做业务聚合。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderReportService {

    private static final int DEFAULT_TOP_N = 10;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final FinanceOrderLedgerEntryFactory ledgerEntryFactory;

    public LoanOverviewVo getLoanOverview(Long bookId) {
        List<FortuneFinanceOrderEntity> orders = orderRepo.listLoanOverviewOrders(bookId);
        Map<String, LoanDetailVo> receivables = new HashMap<>();
        Map<String, LoanDetailVo> payables = new HashMap<>();
        for (FortuneFinanceOrderEntity order : orders) {
            boolean receivable = Objects.equals(order.getType(), FinanceOrderTypeEnum.LOAN_OUT.getValue());
            BigDecimal pending = receivable
                    ? nullSafe(order.getOutAmount()).subtract(nullSafe(order.getInAmount()))
                    .subtract(nullSafe(order.getAdjustedAmount()))
                    : nullSafe(order.getInAmount()).subtract(nullSafe(order.getOutAmount()))
                    .subtract(nullSafe(order.getAdjustedAmount()));
            if (pending.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Map<String, LoanDetailVo> target = receivable ? receivables : payables;
            String key = order.getCounterpartyId() == null
                    ? "name:" + Optional.ofNullable(order.getCounterpartyName())
                    .orElse(FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME)
                    : "id:" + order.getCounterpartyId();
            LoanDetailVo detail = target.computeIfAbsent(key, ignored -> {
                LoanDetailVo value = new LoanDetailVo();
                value.setPayeeName(Optional.ofNullable(order.getCounterpartyName())
                        .orElse(FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME));
                value.setAmount(BigDecimal.ZERO);
                return value;
            });
            detail.setAmount(detail.getAmount().add(pending));
            if (detail.getLastTradeTime() == null || order.getSubmitTime() != null
                    && order.getSubmitTime().isAfter(detail.getLastTradeTime())) {
                detail.setLastTradeTime(order.getSubmitTime());
            }
        }
        List<LoanDetailVo> receivableRows = receivables.values().stream()
                .sorted(Comparator.comparing(LoanDetailVo::getAmount).reversed()).toList();
        List<LoanDetailVo> payableRows = payables.values().stream()
                .sorted(Comparator.comparing(LoanDetailVo::getAmount).reversed()).toList();

        LoanOverviewVo vo = new LoanOverviewVo();
        vo.setTotalReceivable(sumLoanAmount(receivableRows));
        vo.setTotalPayable(sumLoanAmount(payableRows));
        vo.setReceivableCount(receivableRows.size());
        vo.setPayableCount(payableRows.size());
        vo.setTopReceivables(receivableRows.stream().limit(DEFAULT_TOP_N).toList());
        vo.setReviewRequiredCount(orderRepo.countReviewRequired(bookId));
        vo.setOrphanLegacyBillCount(orderRepo.countOrphanBills(bookId));
        return vo;
    }

    public List<FinanceProfitVo> getFinanceProfit(Long bookId, FinanceProfitQuery query) {
        List<FortuneFinanceOrderEntity> orders = orderRepo.listClosedLoanProfitOrders(bookId, query);
        if (orders.isEmpty()) {
            return List.of();
        }
        Map<Long, List<FinanceOrderLedgerEntry>> entriesByOrderId = ledgerEntriesByOrderId(orders);
        return orders.stream()
                .map(order -> profitVo(order, entriesByOrderId.getOrDefault(order.getOrderId(), List.of())))
                .toList();
    }

    private Map<Long, List<FinanceOrderLedgerEntry>> ledgerEntriesByOrderId(
            List<FortuneFinanceOrderEntity> orders) {
        List<Long> orderIds = orders.stream().map(FortuneFinanceOrderEntity::getOrderId).toList();
        return billRepo.listByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(FortuneBillEntity::getOrderId,
                        Collectors.mapping(ledgerEntryFactory::from, Collectors.toList())));
    }

    private FinanceProfitVo profitVo(FortuneFinanceOrderEntity order, List<FinanceOrderLedgerEntry> entries) {
        FinanceOrderLedgerSnapshot snapshot = ledgerCalculator.calculate(order.getType(), entries);
        FinanceProfitVo vo = new FinanceProfitVo();
        vo.setOrderId(order.getOrderId());
        vo.setTitle(order.getTitle());
        vo.setOutAmount(snapshot.outAmount());
        vo.setInAmount(snapshot.inAmount());
        vo.setProfit(snapshot.netCostOrIncome());
        vo.setProfitRate(snapshot.issuedAmount().compareTo(BigDecimal.ZERO) > 0
                ? snapshot.netCostOrIncome().divide(snapshot.issuedAmount(), 2, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED) : BigDecimal.ZERO);
        return vo;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal sumLoanAmount(List<LoanDetailVo> details) {
        if (CollectionUtils.isEmpty(details)) {
            return BigDecimal.ZERO;
        }
        return details.stream().map(LoanDetailVo::getAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
