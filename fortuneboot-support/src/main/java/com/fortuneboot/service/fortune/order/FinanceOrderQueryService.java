package com.fortuneboot.service.fortune.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.mybatis.WrapperUtil;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper.FinanceOrderSummaryRow;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagRelationEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderEntryQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderOptionQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.vo.fortune.FortuneMemberVo;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillExtraVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderAuditVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderCategoryAmountVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderEntryVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderSummaryVo;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import com.fortuneboot.service.permission.FortunePermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 单据查询服务：详情、分页、明细、看板汇总、选择器和审计。
 * <p>列表批量聚合流水，避免N+1；看板汇总走数据库聚合，不基于当前分页求和。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderQueryService {

    private static final Set<Integer> FORMATION_BILL_TYPES = Set.of(7, 9, 11);
    private static final Set<Integer> SETTLEMENT_BILL_TYPES = Set.of(8, 10, 12);
    private static final Set<Integer> VALID_BILL_TYPES = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

    private final FortuneBookRepo bookRepo;
    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FortunePayeeRepo payeeRepo;
    private final FortuneAccountRepo accountRepo;
    private final FortuneCategoryRepo categoryRepo;
    private final FortuneCategoryRelationRepo categoryRelationRepo;
    private final FortuneTagRepo tagRepo;
    private final FortuneTagRelationRepo tagRelationRepo;
    private final FortuneMemberRepo memberRepo;
    private final FortuneMemberRelationRepo memberRelationRepo;
    private final FortuneFileRepo fileRepo;
    private final FortuneBillExtraRepo billExtraRepo;
    private final FortuneFinanceOrderAuditRepo auditRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final ObjectProvider<FortunePermissionService> permissionServiceProvider;
    private final Clock clock;

    public FinanceOrderDetailVo detail(Long bookId, Long orderId) {
        accessGuard.loadBook(bookId);
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(bookId, orderId);
        FinanceOrderLedgerSnapshot snapshot = snapshotOf(order, entriesByOrderId(List.of(orderId))
                .getOrDefault(orderId, List.of()));
        boolean hasBills = billRepo.existAnyByOrderIdIncludeDeleted(orderId);
        return buildDetailVo(order, snapshot, hasBills, isActor(bookId));
    }

    public PageDTO<FinanceOrderDetailVo> page(FinanceOrderPageQuery query) {
        accessGuard.loadBook(query.getBookId());
        LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper = orderWrapper(query.getBookId());
        wrapper.eq(Objects.nonNull(query.getType()), FortuneFinanceOrderEntity::getType, query.getType())
                .eq(Objects.nonNull(query.getStatus()), FortuneFinanceOrderEntity::getStatus, query.getStatus());
        applyKeyword(wrapper, query.getKeyword());

        List<FortuneFinanceOrderEntity> orders = orderRepo.list(wrapper);
        orders.sort(Comparator
                .comparing(FortuneFinanceOrderEntity::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(FortuneFinanceOrderEntity::getUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FortuneFinanceOrderEntity::getOrderId, Comparator.reverseOrder()));

        boolean actor = isActor(query.getBookId());
        Map<Long, List<FinanceOrderLedgerEntry>> entries = entriesByOrderId(
                orders.stream().map(FortuneFinanceOrderEntity::getOrderId).toList());
        List<FinanceOrderDetailVo> all = orders.stream()
                .map(order -> buildDetailVo(order, snapshotOf(order, entries.get(order.getOrderId())), null, actor))
                .filter(vo -> matchesSettlementState(vo, query.getSettlementState()))
                .filter(vo -> !Boolean.TRUE.equals(query.getOverdueOnly()) || vo.isOverdue())
                .toList();

        List<FinanceOrderDetailVo> rows = paginate(all, query.getPageNum(), query.getPageSize());
        fillCanDelete(query.getBookId(), rows, actor);
        return new PageDTO<>(rows, (long) all.size());
    }

    public PageDTO<FinanceOrderEntryVo> entries(FinanceOrderEntryQuery query) {
        accessGuard.loadBook(query.getBookId());
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(query.getBookId(), query.getOrderId());
        LambdaQueryWrapper<FortuneBillEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        wrapper.eq(FortuneBillEntity::getOrderId, order.getOrderId());
        if (query.getEntryStatus() == FinanceOrderEntryQuery.EntryStatus.ACTIVE) {
            wrapper.eq(FortuneBillEntity::getRecycleBin, false);
        } else if (query.getEntryStatus() == FinanceOrderEntryQuery.EntryStatus.VOIDED) {
            wrapper.eq(FortuneBillEntity::getRecycleBin, true);
        }
        wrapper.orderByDesc(FortuneBillEntity::getTradeTime)
                .orderByDesc(FortuneBillEntity::getBillId);
        Page<FortuneBillEntity> page = billRepo.page(new Page<>(
                pageNum(query.getPageNum()), pageSize(query.getPageSize())), wrapper);

        List<FinanceOrderEntryVo> rows = page.getRecords().stream()
                .map(bill -> buildEntryVo(order, bill, isActor(query.getBookId())))
                .toList();
        fillEntryBusinessFields(query.getBookId(), rows);
        return new PageDTO<>(rows, page.getTotal());
    }

    public PageDTO<FinanceOrderAuditVo> audit(Long bookId, Long orderId, Integer pageNum, Integer pageSize) {
        accessGuard.loadBook(bookId);
        accessGuard.loadOrder(bookId, orderId);
        LambdaQueryWrapper<FortuneFinanceOrderAuditEntity> wrapper =
                new LambdaQueryWrapper<>(FortuneFinanceOrderAuditEntity.class);
        wrapper.eq(FortuneFinanceOrderAuditEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderAuditEntity::getOrderId, orderId)
                .orderByDesc(FortuneFinanceOrderAuditEntity::getCreateTime)
                .orderByDesc(FortuneFinanceOrderAuditEntity::getId);
        Page<FortuneFinanceOrderAuditEntity> page = auditRepo.page(new Page<>(pageNum(pageNum), pageSize(pageSize)),
                wrapper);
        List<FinanceOrderAuditVo> rows = page.getRecords().stream().map(this::buildAuditVo).toList();
        return new PageDTO<>(rows, page.getTotal());
    }

    public FinanceOrderSummaryVo summary(Long bookId) {
        FortuneBookEntity book = accessGuard.loadBook(bookId);
        LocalDate today = LocalDate.now(clock);
        FinanceOrderSummaryRow row = orderRepo.summarizeReadyOrders(bookId, today);

        FinanceOrderSummaryVo vo = new FinanceOrderSummaryVo();
        vo.setCurrencyCode(book.getDefaultCurrency());
        vo.setReceivableAmount(formatMoney(row.getReceivableAmount()));
        vo.setPayableAmount(formatMoney(row.getPayableAmount()));
        vo.setReimbursementAmount(formatMoney(row.getReimbursementAmount()));
        vo.setOverdueReceivableAmount(formatMoney(row.getOverdueReceivableAmount()));
        vo.setOverduePayableAmount(formatMoney(row.getOverduePayableAmount()));
        vo.setOverdueReimbursementAmount(formatMoney(row.getOverdueReimbursementAmount()));
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
        Integer billType = query.getBillType();
        if (!VALID_BILL_TYPES.contains(billType)) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "流水类型非法: " + billType);
        }
        boolean formation = FORMATION_BILL_TYPES.contains(billType);
        if (!formation && !SETTLEMENT_BILL_TYPES.contains(billType)) {
            return new PageDTO<>(List.of(), 0L);
        }
        Integer orderType = billType <= 8 ? FinanceOrderTypeEnum.EXPENSE_CLAIM.getValue()
                : billType <= 10 ? FinanceOrderTypeEnum.LOAN_OUT.getValue()
                : FinanceOrderTypeEnum.LOAN_IN.getValue();

        LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper = orderWrapper(query.getBookId());
        wrapper.eq(FortuneFinanceOrderEntity::getType, orderType)
                .eq(FortuneFinanceOrderEntity::getReconciliationStatus,
                        FinanceOrderReconciliationStatusEnum.READY.getValue());
        if (formation) {
            wrapper.in(FortuneFinanceOrderEntity::getStatus,
                    FinanceOrderStatusEnum.INIT.getValue(), FinanceOrderStatusEnum.USING.getValue());
        } else {
            wrapper.eq(FortuneFinanceOrderEntity::getStatus, FinanceOrderStatusEnum.USING.getValue());
        }
        applyKeyword(wrapper, query.getKeyword());

        List<FortuneFinanceOrderEntity> orders = orderRepo.list(wrapper);
        orders.sort(Comparator
                .comparing(FortuneFinanceOrderEntity::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(FortuneFinanceOrderEntity::getUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FortuneFinanceOrderEntity::getOrderId, Comparator.reverseOrder()));

        boolean actor = isActor(query.getBookId());
        Map<Long, List<FinanceOrderLedgerEntry>> entries = entriesByOrderId(
                orders.stream().map(FortuneFinanceOrderEntity::getOrderId).toList());
        List<FinanceOrderDetailVo> all = orders.stream()
                .map(order -> buildDetailVo(order, snapshotOf(order, entries.get(order.getOrderId())), null, actor))
                .filter(vo -> formation || parseMoney(vo.getPendingAmount()).compareTo(BigDecimal.ZERO) > 0)
                .toList();
        List<FinanceOrderDetailVo> rows = paginate(all, query.getPageNum(), query.getPageSize());
        fillCanDelete(query.getBookId(), rows, actor);
        return new PageDTO<>(rows, (long) all.size());
    }

    private LambdaQueryWrapper<FortuneFinanceOrderEntity> orderWrapper(Long bookId) {
        LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper =
                WrapperUtil.getLambdaQueryWrapper(FortuneFinanceOrderEntity.class);
        wrapper.eq(FortuneFinanceOrderEntity::getBookId, bookId);
        return wrapper;
    }

    private void applyKeyword(LambdaQueryWrapper<FortuneFinanceOrderEntity> wrapper, String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(condition -> condition
                    .like(FortuneFinanceOrderEntity::getTitle, keyword)
                    .or()
                    .like(FortuneFinanceOrderEntity::getCounterpartyName, keyword));
        }
    }

    private Map<Long, List<FinanceOrderLedgerEntry>> entriesByOrderId(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Map.of();
        }
        LambdaQueryWrapper<FortuneBillEntity> wrapper = WrapperUtil.getLambdaQueryWrapper(FortuneBillEntity.class);
        wrapper.in(FortuneBillEntity::getOrderId, orderIds);
        return billRepo.list(wrapper).stream().collect(Collectors.groupingBy(FortuneBillEntity::getOrderId,
                Collectors.mapping(this::ledgerEntry, Collectors.toList())));
    }

    private FinanceOrderLedgerEntry ledgerEntry(FortuneBillEntity bill) {
        return new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(), bill.getOrderComponent(),
                bill.getAmount(), bill.getTradeTime(), Boolean.TRUE.equals(bill.getConfirm()),
                Boolean.TRUE.equals(bill.getDeleted()), Boolean.TRUE.equals(bill.getRecycleBin()),
                bill.getOrderLedgerVersion() == null ? 0 : bill.getOrderLedgerVersion());
    }

    private FinanceOrderLedgerSnapshot snapshotOf(FortuneFinanceOrderEntity order,
                                                   List<FinanceOrderLedgerEntry> entries) {
        if (!FinanceOrderReconciliationStatusEnum.READY.getValue().equals(order.getReconciliationStatus())) {
            return null;
        }
        try {
            return ledgerCalculator.calculate(order.getType(), entries);
        } catch (ApiException exception) {
            return null;
        }
    }

    private FinanceOrderDetailVo buildDetailVo(FortuneFinanceOrderEntity order,
                                               FinanceOrderLedgerSnapshot snapshot,
                                               Boolean hasBills,
                                               boolean actor) {
        boolean ready = FinanceOrderReconciliationStatusEnum.READY.getValue()
                .equals(order.getReconciliationStatus());
        FinanceOrderDetailVo vo = new FinanceOrderDetailVo();
        vo.setOrderId(order.getOrderId());
        vo.setBookId(order.getBookId());
        vo.setTitle(order.getTitle());
        vo.setType(order.getType());
        vo.setTypeDesc(FinanceOrderTypeEnum.getDescByValue(order.getType()));
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(FinanceOrderStatusEnum.getDescByValue(order.getStatus()));
        vo.setCounterpartyId(order.getCounterpartyId());
        vo.setCounterpartyName(order.getCounterpartyName());
        vo.setCurrencyCode(order.getCurrencyCode());
        vo.setDueDate(order.getDueDate());
        vo.setSubmitTime(order.getSubmitTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setRemark(order.getRemark());
        vo.setVersion(order.getVersion());
        vo.setReconciliationStatus(order.getReconciliationStatus());
        vo.setReconciliationNote(order.getReconciliationNote());

        if (ready && snapshot != null) {
            vo.setOutAmount(formatMoney(snapshot.outAmount()));
            vo.setInAmount(formatMoney(snapshot.inAmount()));
            vo.setIssuedAmount(formatMoney(snapshot.issuedAmount()));
            vo.setSettledAmount(formatMoney(snapshot.settledAmount()));
            vo.setAdjustedAmount(formatMoney(snapshot.adjustedAmount()));
            vo.setPendingAmount(formatMoney(snapshot.pendingAmount()));
            vo.setInterestIncome(formatMoney(snapshot.interestIncome()));
            vo.setInterestExpense(formatMoney(snapshot.interestExpense()));
            vo.setFeeAmount(formatMoney(snapshot.feeAmount()));
            vo.setNetCostOrIncome(formatMoney(snapshot.netCostOrIncome()));
            vo.setSettlementState(snapshot.settlementState());
            vo.setPendingEntryCount(snapshot.pendingEntryCount());
        } else {
            vo.setSettlementState(FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue());
            vo.setReadOnlyReason(StringUtils.defaultIfBlank(order.getReconciliationNote(),
                    FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue()));
        }

        boolean editableStatus = Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue())
                || Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue());
        boolean pendingZero = snapshot == null || snapshot.pendingAmount().compareTo(BigDecimal.ZERO) == 0;
        boolean issued = snapshot != null && snapshot.issuedAmount().compareTo(BigDecimal.ZERO) > 0;
        vo.setCanEdit(actor && ready && editableStatus);
        vo.setCanDelete(actor && ready && editableStatus && !Boolean.TRUE.equals(hasBills));
        vo.setCanClose(actor && ready && Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())
                && issued && pendingZero && (snapshot == null || snapshot.pendingEntryCount() == 0));
        vo.setCanReopen(actor && ready
                && Objects.equals(order.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue()));
        vo.setAllowedEntryActions(orderAllowedEntryActions(order, actor, ready));
        vo.setOverdue(order.getDueDate() != null
                && order.getDueDate().isBefore(LocalDate.now(clock))
                && snapshot != null
                && snapshot.pendingAmount().compareTo(BigDecimal.ZERO) > 0);
        return vo;
    }

    private String[] orderAllowedEntryActions(FortuneFinanceOrderEntity order, boolean actor, boolean ready) {
        if (!actor || !ready) {
            return new String[0];
        }
        if (Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue())) {
            return switch (order.getType()) {
                case 1 -> new String[]{"ADVANCE"};
                case 2 -> new String[]{"LOAN_OUT"};
                case 3 -> new String[]{"LOAN_IN"};
                default -> new String[0];
            };
        }
        if (Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue())) {
            return new String[]{"COPY"};
        }
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            return new String[0];
        }
        return switch (order.getType()) {
            case 1 -> new String[]{"ADVANCE", "REIMBURSE", "FEE", "WRITE_OFF"};
            case 2 -> new String[]{"LOAN_OUT", "LOAN_RECOVER", "INTEREST_IN", "FEE", "WRITE_OFF"};
            case 3 -> new String[]{"LOAN_IN", "LOAN_REPAY", "INTEREST_OUT", "FEE", "WRITE_OFF"};
            default -> new String[0];
        };
    }

    private boolean matchesSettlementState(FinanceOrderDetailVo vo, String settlementState) {
        return StringUtils.isBlank(settlementState) || Objects.equals(vo.getSettlementState(), settlementState);
    }

    private List<FinanceOrderDetailVo> paginate(List<FinanceOrderDetailVo> all, Integer pageNum, Integer pageSize) {
        int from = (pageNum(pageNum) - 1) * pageSize(pageSize);
        if (from >= all.size()) {
            return List.of();
        }
        int to = Math.min(from + pageSize(pageSize), all.size());
        return new ArrayList<>(all.subList(from, to));
    }

    private void fillCanDelete(Long bookId, List<FinanceOrderDetailVo> rows, boolean actor) {
        if (!actor) {
            return;
        }
        List<Long> orderIds = rows.stream()
                .filter(vo -> vo.isCanDelete())
                .map(FinanceOrderDetailVo::getOrderId)
                .toList();
        Set<Long> withBills = new HashSet<>(billRepo.listOrderIdsWithAnyBillIncludeDeleted(orderIds));
        rows.stream()
                .filter(vo -> vo.isCanDelete())
                .forEach(vo -> vo.setCanDelete(!withBills.contains(vo.getOrderId())));
    }

    private FinanceOrderEntryVo buildEntryVo(FortuneFinanceOrderEntity order, FortuneBillEntity bill, boolean actor) {
        FinanceOrderEntryVo vo = new FinanceOrderEntryVo();
        vo.setBillId(bill.getBillId());
        vo.setBookId(bill.getBookId());
        vo.setTitle(bill.getTitle());
        vo.setTradeTime(bill.getTradeTime());
        vo.setAccountId(bill.getAccountId());
        vo.setOrderId(bill.getOrderId());
        vo.setAmount(formatMoney(bill.getAmount()));
        vo.setConvertedAmount(formatMoney(bill.getConvertedAmount()));
        vo.setPayeeId(bill.getPayeeId());
        vo.setBillType(bill.getBillType());
        vo.setToAccountId(bill.getToAccountId());
        vo.setConfirm(bill.getConfirm());
        vo.setInclude(bill.getInclude());
        vo.setRemark(bill.getRemark());
        vo.setOrderComponent(bill.getOrderComponent());
        vo.setOrderLedgerVersion(bill.getOrderLedgerVersion() == null ? 0 : bill.getOrderLedgerVersion());
        vo.setRecycleBin(Boolean.TRUE.equals(bill.getRecycleBin()));
        vo.setAllowedActions(entryAllowedActions(order, bill, actor));
        if (!FinanceOrderReconciliationStatusEnum.READY.getValue().equals(order.getReconciliationStatus())) {
            vo.setReadOnlyReason(StringUtils.defaultIfBlank(order.getReconciliationNote(),
                    FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue()));
        }
        return vo;
    }

    private String[] entryAllowedActions(FortuneFinanceOrderEntity order, FortuneBillEntity bill, boolean actor) {
        if (!actor || !FinanceOrderReconciliationStatusEnum.READY.getValue()
                .equals(order.getReconciliationStatus())) {
            return new String[0];
        }
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            return new String[0];
        }
        List<String> actions = new ArrayList<>();
        boolean voided = Boolean.TRUE.equals(bill.getRecycleBin());
        if (!voided) {
            actions.add("EDIT");
            actions.add(Boolean.TRUE.equals(bill.getConfirm()) ? "UNCONFIRM" : "CONFIRM");
            actions.add("VOID");
        } else {
            actions.add("RESTORE");
        }
        actions.add("COPY");
        return actions.toArray(String[]::new);
    }

    private void fillEntryBusinessFields(Long bookId, List<FinanceOrderEntryVo> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<Long> billIds = rows.stream().map(FinanceOrderEntryVo::getBillId).toList();
        Map<Long, List<FortuneCategoryRelationEntity>> categoryRelations =
                categoryRelationRepo.getByBillIdList(billIds);
        Map<Long, List<FortuneTagRelationEntity>> tagRelations = tagRelationRepo.getByBillIdList(billIds);
        Map<Long, List<FortuneMemberRelationEntity>> memberRelations = memberRelationRepo.getByBillIdList(billIds);
        Map<Long, List<FortuneBillExtraEntity>> extras = billExtraRepo.getByBillIdList(billIds);
        Set<Long> hasFileBillIds = fileRepo.findBillIdsWithFiles(billIds);

        Set<Long> categoryIds = categoryRelations.values().stream().flatMap(List::stream)
                .map(FortuneCategoryRelationEntity::getCategoryId).collect(Collectors.toSet());
        Set<Long> tagIds = tagRelations.values().stream().flatMap(List::stream)
                .map(FortuneTagRelationEntity::getTagId).collect(Collectors.toSet());
        Set<Long> memberIds = memberRelations.values().stream().flatMap(List::stream)
                .map(FortuneMemberRelationEntity::getMemberId).collect(Collectors.toSet());
        Set<Long> accountIds = rows.stream().flatMap(row -> List.of(
                Optional.ofNullable(row.getAccountId()).orElse(-1L),
                Optional.ofNullable(row.getToAccountId()).orElse(-1L)).stream()).collect(Collectors.toSet());
        Set<Long> payeeIds = rows.stream().map(FinanceOrderEntryVo::getPayeeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, FortuneCategoryEntity> categories = categoryRepo.getByIds(new ArrayList<>(categoryIds)).stream()
                .collect(Collectors.toMap(FortuneCategoryEntity::getCategoryId, Function.identity()));
        Map<Long, FortuneTagEntity> tags = tagRepo.getByIds(new ArrayList<>(tagIds)).stream()
                .collect(Collectors.toMap(FortuneTagEntity::getTagId, Function.identity()));
        Map<Long, FortuneMemberEntity> members = memberRepo.getByIds(new ArrayList<>(memberIds)).stream()
                .collect(Collectors.toMap(FortuneMemberEntity::getMemberId, Function.identity()));
        Map<Long, FortuneAccountEntity> accounts = accountRepo.getByIds(new ArrayList<>(accountIds)).stream()
                .collect(Collectors.toMap(FortuneAccountEntity::getAccountId, Function.identity()));
        Map<Long, String> payeeNames = payeeRepo.getByIdList(new ArrayList<>(payeeIds)).stream()
                .collect(Collectors.toMap(FortunePayeeEntity::getPayeeId, FortunePayeeEntity::getPayeeName));
        Map<Long, FortuneBookEntity> books = bookRepo.listByIds(List.of(bookId)).stream()
                .collect(Collectors.toMap(FortuneBookEntity::getBookId, Function.identity()));

        for (FinanceOrderEntryVo row : rows) {
            FortuneBookEntity book = books.get(bookId);
            if (book != null) {
                row.setBookName(book.getBookName());
            }
            FortuneAccountEntity account = accounts.get(row.getAccountId());
            if (account != null) {
                row.setAccountName(account.getAccountName());
                row.setCurrencyCode(account.getCurrencyCode());
            }
            FortuneAccountEntity toAccount = accounts.get(row.getToAccountId());
            if (toAccount != null) {
                row.setToAccountName(toAccount.getAccountName());
                row.setToCurrencyCode(toAccount.getCurrencyCode());
            }
            if (row.getPayeeId() != null) {
                row.setPayeeName(payeeNames.get(row.getPayeeId()));
            }
            row.setHasFile(hasFileBillIds.contains(row.getBillId()));
            row.setCategoryAmountPair(categoryAmounts(categoryRelations.get(row.getBillId()), categories));
            row.setTagList(relationsOf(tagRelations.get(row.getBillId()),
                    relation -> tags.get(relation.getTagId()) == null ? null
                            : new FortuneTagVo(tags.get(relation.getTagId()))));
            row.setMemberList(relationsOf(memberRelations.get(row.getBillId()),
                    relation -> members.get(relation.getMemberId()) == null ? null
                            : new FortuneMemberVo(members.get(relation.getMemberId()))));
            row.setExtras(extras.get(row.getBillId()) == null ? List.of()
                    : extras.get(row.getBillId()).stream().map(FortuneBillExtraVo::new).toList());
        }
    }

    private List<FinanceOrderCategoryAmountVo> categoryAmounts(List<FortuneCategoryRelationEntity> relations,
                                                               Map<Long, FortuneCategoryEntity> categories) {
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        return relations.stream().map(relation -> {
            FortuneCategoryEntity category = categories.get(relation.getCategoryId());
            FinanceOrderCategoryAmountVo vo = new FinanceOrderCategoryAmountVo();
            vo.setCategoryId(relation.getCategoryId());
            vo.setCategoryName(category == null ? null : category.getCategoryName());
            vo.setAmount(formatMoney(relation.getAmount()));
            return vo;
        }).toList();
    }

    private <R, V> List<V> relationsOf(List<R> relations, Function<R, V> mapper) {
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        return relations.stream().map(mapper).filter(Objects::nonNull).toList();
    }

    private FinanceOrderAuditVo buildAuditVo(FortuneFinanceOrderAuditEntity audit) {
        FinanceOrderAuditVo vo = new FinanceOrderAuditVo();
        vo.setId(audit.getId());
        vo.setOrderId(audit.getOrderId());
        vo.setBillId(audit.getBillId());
        vo.setAction(audit.getAction());
        vo.setCreatorId(audit.getCreatorId());
        vo.setCreateTime(audit.getCreateTime());
        vo.setBeforeJson(audit.getBeforeJson());
        vo.setAfterJson(audit.getAfterJson());
        return vo;
    }

    private boolean isActor(Long bookId) {
        try {
            FortunePermissionService permissionService = permissionServiceProvider.getIfAvailable();
            return permissionService != null
                    && Boolean.TRUE.equals(permissionService.bookActorPermission(bookId));
        } catch (Exception exception) {
            return false;
        }
    }

    private int pageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? FinanceOrderPageQuery.DEFAULT_PAGE_NUM : pageNum;
    }

    private int pageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? FinanceOrderPageQuery.DEFAULT_PAGE_SIZE
                : Math.min(pageSize, FinanceOrderPageQuery.MAX_PAGE_SIZE);
    }

    private BigDecimal parseMoney(String amount) {
        return amount == null ? BigDecimal.ZERO : new BigDecimal(amount);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
