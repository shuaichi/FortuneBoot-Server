package com.fortuneboot.service.fortune.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderSummaryRow;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderOptionQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageUtil;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderAuditVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderEntryVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderSummaryVo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.service.permission.FortunePermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单据查询服务：详情、分页、明细、看板汇总、选择器和审计。
 * <p>Service只负责权限、编排和业务过滤；查询条件由Repository封装，VO转换由Assembler承接。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderQueryService {

    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FortuneFinanceOrderAuditRepo auditRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final ObjectProvider<FortunePermissionService> permissionServiceProvider;
    private final Clock clock;
    private final FinanceOrderDetailAssembler detailAssembler;
    private final FinanceOrderEntryAssembler entryAssembler;
    private final FinanceOrderAuditAssembler auditAssembler;
    private final FinanceOrderLedgerEntryFactory ledgerEntryFactory;

    public FinanceOrderDetailVo detail(Long bookId, Long orderId) {
        accessGuard.loadBook(bookId);
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(bookId, orderId);
        FinanceOrderLedgerSnapshot snapshot = snapshotOf(order, entriesByOrderId(List.of(orderId))
                .getOrDefault(orderId, List.of()));
        boolean hasBills = billRepo.existAnyByOrderIdIncludeDeleted(orderId);
        return detailAssembler.build(order, snapshot, hasBills, isActor(bookId));
    }

    public PageDTO<FinanceOrderDetailVo> page(FinanceOrderPageQuery query) {
        accessGuard.loadBook(query.getBookId());
        List<FortuneFinanceOrderEntity> orders = orderRepo.listByPageQuery(query);

        boolean actor = isActor(query.getBookId());
        Map<Long, List<FinanceOrderLedgerEntry>> entries = entriesByOrderId(
                orders.stream().map(FortuneFinanceOrderEntity::getOrderId).toList());
        List<FinanceOrderDetailVo> all = orders.stream()
                .map(order -> detailAssembler.build(order,
                        snapshotOf(order, entries.get(order.getOrderId())), null, actor))
                .filter(vo -> matchesSettlementState(vo, query.getSettlementState()))
                .filter(vo -> !Boolean.TRUE.equals(query.getOverdueOnly()) || vo.isOverdue())
                .toList();

        List<FinanceOrderDetailVo> rows = FinanceOrderPageUtil.paginate(all, query.getPageNum(), query.getPageSize());
        fillCanDelete(rows, actor);
        return new PageDTO<>(rows, (long) all.size());
    }

    public PageDTO<FinanceOrderEntryVo> entries(FinanceOrderEntryQuery query) {
        accessGuard.loadBook(query.getBookId());
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(query.getBookId(), query.getOrderId());
        Page<FortuneBillEntity> page = billRepo.pageByEntryQuery(query);

        List<FinanceOrderEntryVo> rows = page.getRecords().stream()
                .map(bill -> entryAssembler.build(order, bill, isActor(query.getBookId())))
                .toList();
        entryAssembler.fillBusinessFields(query.getBookId(), rows);
        return new PageDTO<>(rows, page.getTotal());
    }

    public PageDTO<FinanceOrderAuditVo> audit(Long bookId, Long orderId, Integer pageNum, Integer pageSize) {
        accessGuard.loadBook(bookId);
        accessGuard.loadOrder(bookId, orderId);
        Page<FortuneFinanceOrderAuditEntity> page = auditRepo.pageByOrder(bookId, orderId, pageNum, pageSize);
        List<FinanceOrderAuditVo> rows = page.getRecords().stream()
                .map(auditAssembler::toVo)
                .toList();
        return new PageDTO<>(rows, page.getTotal());
    }

    public FinanceOrderSummaryVo summary(Long bookId) {
        FortuneBookEntity book = accessGuard.loadBook(bookId);
        LocalDate today = LocalDate.now(clock);
        FinanceOrderSummaryRow row = orderRepo.summarizeReadyOrders(bookId, today);

        FinanceOrderSummaryVo vo = new FinanceOrderSummaryVo();
        vo.setCurrencyCode(book.getDefaultCurrency());
        vo.setReceivableAmount(FinanceOrderAmountRule.format(row.getReceivableAmount()));
        vo.setPayableAmount(FinanceOrderAmountRule.format(row.getPayableAmount()));
        vo.setReimbursementAmount(FinanceOrderAmountRule.format(row.getReimbursementAmount()));
        vo.setOverdueReceivableAmount(FinanceOrderAmountRule.format(row.getOverdueReceivableAmount()));
        vo.setOverduePayableAmount(FinanceOrderAmountRule.format(row.getOverduePayableAmount()));
        vo.setOverdueReimbursementAmount(FinanceOrderAmountRule.format(row.getOverdueReimbursementAmount()));
        vo.setReceivableCount((int) row.getReceivableCount());
        vo.setPayableCount((int) row.getPayableCount());
        vo.setReimbursementCount((int) row.getReimbursementCount());
        vo.setPendingEntryCount(orderRepo.countPendingReadyEntries(bookId));
        vo.setReviewRequiredCount(orderRepo.countReviewRequired(bookId));
        vo.setOrphanLegacyBillCount(orderRepo.countOrphanBills(bookId));
        vo.setAsOfDate(today);
        return vo;
    }

    public PageDTO<FinanceOrderDetailVo> options(FinanceOrderOptionQuery query) {
        accessGuard.loadBook(query.getBookId());
        BillTypeEnum billType = BillTypeEnum.getByValue(query.getBillType());
        if (billType == null) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "流水类型非法: " + query.getBillType());
        }
        FinanceOrderTypeEnum orderType = FinanceOrderTypeEnum.fromBillType(billType);
        if (orderType == null) {
            return new PageDTO<>(List.of(), 0L);
        }
        boolean formation = billType.isOrderFormation();

        List<FortuneFinanceOrderEntity> orders = orderRepo.listByOptionQuery(query);
        boolean actor = isActor(query.getBookId());
        Map<Long, List<FinanceOrderLedgerEntry>> entries = entriesByOrderId(
                orders.stream().map(FortuneFinanceOrderEntity::getOrderId).toList());
        List<FinanceOrderDetailVo> all = orders.stream()
                .map(order -> detailAssembler.build(order,
                        snapshotOf(order, entries.get(order.getOrderId())), null, actor))
                .filter(vo -> formation || FinanceOrderAmountRule.parse(vo.getPendingAmount())
                        .compareTo(BigDecimal.ZERO) > 0)
                .toList();
        List<FinanceOrderDetailVo> rows = FinanceOrderPageUtil.paginate(all, query.getPageNum(), query.getPageSize());
        fillCanDelete(rows, actor);
        return new PageDTO<>(rows, (long) all.size());
    }

    private Map<Long, List<FinanceOrderLedgerEntry>> entriesByOrderId(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Map.of();
        }
        return billRepo.listByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(FortuneBillEntity::getOrderId,
                        Collectors.mapping(ledgerEntryFactory::from, Collectors.toList())));
    }

    private FinanceOrderLedgerSnapshot snapshotOf(FortuneFinanceOrderEntity order,
                                                  List<FinanceOrderLedgerEntry> entries) {
        if (!FinanceOrderReconciliationStatusEnum.isReadyValue(order.getReconciliationStatus())) {
            return null;
        }
        try {
            return ledgerCalculator.calculate(order.getType(), entries);
        } catch (ApiException exception) {
            return null;
        }
    }

    private boolean matchesSettlementState(FinanceOrderDetailVo vo, String settlementState) {
        return StringUtils.isBlank(settlementState) || Objects.equals(vo.getSettlementState(), settlementState);
    }

    private void fillCanDelete(List<FinanceOrderDetailVo> rows, boolean actor) {
        if (!actor) {
            return;
        }
        List<Long> orderIds = rows.stream()
                .filter(FinanceOrderDetailVo::isCanDelete)
                .map(FinanceOrderDetailVo::getOrderId)
                .toList();
        Set<Long> withBills = new HashSet<>(billRepo.listOrderIdsWithAnyBillIncludeDeleted(orderIds));
        rows.stream()
                .filter(FinanceOrderDetailVo::isCanDelete)
                .forEach(vo -> vo.setCanDelete(!withBills.contains(vo.getOrderId())));
    }

    private boolean isActor(Long bookId) {
        try {
            FortunePermissionService permissionService = permissionServiceProvider.getIfAvailable();
            return permissionService != null && Boolean.TRUE.equals(permissionService.bookActorPermission(bookId));
        } catch (Exception exception) {
            return false;
        }
    }
}
