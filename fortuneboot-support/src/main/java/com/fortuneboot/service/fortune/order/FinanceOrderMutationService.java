package com.fortuneboot.service.fortune.order;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderRequestEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRequestRepo;
import com.fortuneboot.service.fortune.FortuneBalanceSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class FinanceOrderMutationService {

    private final FortuneBookRepo bookRepo;
    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FortuneAccountRepo accountRepo;
    private final FortuneFinanceOrderRequestRepo requestRepo;
    private final FortuneFinanceOrderAuditRepo auditRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderScopeLock scopeLock;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final FinanceOrderCashEffectCalculator cashEffectCalculator;
    private final FinanceOrderEntryPersistence entryPersistence;
    private final FortuneBalanceSnapshotService snapshotService;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeEntry(FinanceOrderEntryMutation command, String requestHash) {
        insertRequestPlaceholder(command.getBookId(), command.getRequestId(), command.getOperation().name(),
                requestHash);

        FortuneBookEntity bookBeforeLock = accessGuard.loadBook(command.getBookId());
        scopeLock.lockGroup(bookBeforeLock.getGroupId());

        FortuneBookEntity book = accessGuard.loadBook(command.getBookId());
        boolean allowDraft = command.getOperation() == FinanceOrderEntryMutation.Operation.ADD;
        FortuneFinanceOrderEntity order = accessGuard.loadMutableOrder(book, command.getOrderId(), allowDraft);

        FortuneBillEntity oldBill = null;
        if (command.getOperation() != FinanceOrderEntryMutation.Operation.ADD) {
            oldBill = accessGuard.loadBill(order, command.getBillId());
            validateOperation(oldBill, command.getOperation());
        }

        if (isNoEffectOperation(oldBill, command.getOperation())) {
            FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), oldBill.getBillId(),
                    order.getVersion(), true);
            saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
            return result;
        }

        long expectedVersion = Objects.requireNonNullElse(command.getExpectedVersion(), order.getVersion());
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        long nextVersion = expectedVersion + 1;
        order = accessGuard.loadMutableOrder(book, command.getOrderId(), true);
        if (order.getVersion() != nextVersion) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }

        FortuneBillEntity targetBill;
        if (command.getOperation() == FinanceOrderEntryMutation.Operation.ADD) {
            FinanceOrderEntryCommand normalized = accessGuard.normalizeCommand(order, command.getEntryCommand());
            targetBill = buildAddBill(normalized);
        } else if (command.getOperation() == FinanceOrderEntryMutation.Operation.MODIFY) {
            FinanceOrderEntryCommand normalized = accessGuard.normalizeCommand(order, command.getEntryCommand());
            validateImmutableFields(oldBill, normalized);
            targetBill = copyBill(oldBill);
            applyCommand(targetBill, normalized);
        } else {
            targetBill = copyBill(oldBill);
            applyState(targetBill, command.getOperation());
        }

        List<FinanceOrderLedgerEntry> targetEntries = targetEntries(order, oldBill, targetBill);
        FinanceOrderLedgerSnapshot targetSnapshot = ledgerCalculator.calculate(order.getType(), targetEntries);

        Map<Long, BigDecimal> oldEffect = cashEffectCalculator.calculate(oldBill);
        Map<Long, BigDecimal> newEffect = cashEffectCalculator.calculate(targetBill);
        validateAccounts(book, order, oldEffect, false);
        validateAccounts(book, order, newEffect, requiresUsableAccount(command.getOperation()));

        applyCashDeltas(oldEffect, newEffect);
        FortuneBillEntity persistedBill = persist(command, oldBill, targetBill);
        rebuildOrderSummary(order, targetSnapshot, command.getOperation(), persistedBill);
        rebuildSnapshots(oldBill, targetBill, oldEffect, newEffect);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), persistedBill.getBillId(),
                nextVersion, false);
        saveAudit(command, oldBill, persistedBill, targetSnapshot);
        saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeInclude(Long bookId, Long billId, boolean include,
                                                 String requestId, String requestHash) {
        insertRequestPlaceholder(bookId, requestId, include ? "INCLUDE" : "EXCLUDE", requestHash);
        FortuneBillEntity billBeforeLock = billRepo.getById(billId);
        if (billBeforeLock == null || billBeforeLock.getOrderId() == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, billId, "流水");
        }
        FortuneBookEntity bookBeforeLock = accessGuard.loadBook(bookId);
        scopeLock.lockGroup(bookBeforeLock.getGroupId());

        FortuneBookEntity book = accessGuard.loadBook(bookId);
        FortuneFinanceOrderEntity order = accessGuard.loadMutableOrder(book, billBeforeLock.getOrderId(), false);
        FortuneBillEntity oldBill = accessGuard.loadBill(order, billId);
        if (Objects.equals(oldBill.getInclude(), include)) {
            FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), billId,
                    order.getVersion(), true);
            saveRequestReceipt(bookId, requestId, requestHash, result);
            return result;
        }

        long expectedVersion = order.getVersion();
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), bookId, expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        order = accessGuard.loadMutableOrder(book, order.getOrderId(), true);
        oldBill.setInclude(include);
        entryPersistence.updateState(oldBill);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), billId,
                expectedVersion + 1, false);
        saveStateAudit(bookId, order.getOrderId(), billId, requestId, include ? "INCLUDE" : "EXCLUDE",
                Map.of("include", !include), Map.of("include", include));
        saveRequestReceipt(bookId, requestId, requestHash, result);
        return result;
    }

    static Map<Long, BigDecimal> mergeDeltas(Map<Long, BigDecimal> oldEffect, Map<Long, BigDecimal> newEffect,
                                              FinanceOrderCashEffectCalculator calculator) {
        Map<Long, BigDecimal> deltas = new TreeMap<>();
        oldEffect.forEach((accountId, amount) -> deltas.merge(accountId, amount.negate(), BigDecimal::add));
        newEffect.forEach((accountId, amount) -> deltas.merge(accountId, amount, BigDecimal::add));
        return deltas;
    }

    private void insertRequestPlaceholder(Long bookId, String requestId, String operation, String requestHash) {
        FortuneFinanceOrderRequestEntity request = new FortuneFinanceOrderRequestEntity();
        request.setBookId(bookId);
        request.setRequestId(requestId);
        request.setOperation(operation);
        request.setRequestHash(requestHash);
        requestRepo.save(request);
    }

    private void validateOperation(FortuneBillEntity bill, FinanceOrderEntryMutation.Operation operation) {
        if (operation == FinanceOrderEntryMutation.Operation.MODIFY
                || operation == FinanceOrderEntryMutation.Operation.CONFIRM
                || operation == FinanceOrderEntryMutation.Operation.UNCONFIRM) {
            if (Boolean.TRUE.equals(bill.getRecycleBin())) {
                throw new ApiException(ErrorCode.Business.ORDER_ENTRY_IMMUTABLE);
            }
        }
    }

    private boolean isNoEffectOperation(FortuneBillEntity bill, FinanceOrderEntryMutation.Operation operation) {
        return switch (operation) {
            case CONFIRM -> Boolean.TRUE.equals(bill.getConfirm());
            case UNCONFIRM -> Boolean.FALSE.equals(bill.getConfirm());
            case VOID -> Boolean.TRUE.equals(bill.getRecycleBin());
            case RESTORE -> Boolean.FALSE.equals(bill.getRecycleBin());
            default -> false;
        };
    }

    private FortuneBillEntity buildAddBill(FinanceOrderEntryCommand command) {
        FortuneBillEntity bill = new FortuneBillEntity();
        BeanUtil.copyProperties(command, bill, "billId", "fileList", "extras", "requestId", "expectedVersion");
        bill.setOrderId(command.getOrderId());
        bill.setOrderComponent(command.getOrderComponent());
        bill.setOrderLedgerVersion(1);
        bill.setDeleted(false);
        bill.setRecycleBin(false);
        return bill;
    }

    private FortuneBillEntity copyBill(FortuneBillEntity source) {
        FortuneBillEntity target = new FortuneBillEntity();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    private void applyCommand(FortuneBillEntity bill, FinanceOrderEntryCommand command) {
        bill.setTitle(command.getTitle());
        bill.setTradeTime(command.getTradeTime());
        bill.setAccountId(command.getAccountId());
        bill.setAmount(command.getAmount());
        bill.setConvertedAmount(command.getConvertedAmount());
        bill.setPayeeId(command.getPayeeId());
        bill.setConfirm(command.getConfirm());
        bill.setInclude(command.getInclude());
        bill.setRemark(command.getRemark());
    }

    private void applyState(FortuneBillEntity bill, FinanceOrderEntryMutation.Operation operation) {
        switch (operation) {
            case CONFIRM -> bill.setConfirm(true);
            case UNCONFIRM -> bill.setConfirm(false);
            case VOID -> bill.setRecycleBin(true);
            case RESTORE -> bill.setRecycleBin(false);
            default -> throw new ApiException(ErrorCode.Business.ORDER_ENTRY_IMMUTABLE);
        }
    }

    private void validateImmutableFields(FortuneBillEntity oldBill, FinanceOrderEntryCommand command) {
        if (!Objects.equals(oldBill.getBillType(), command.getBillType())
                || !Objects.equals(oldBill.getOrderComponent(), command.getOrderComponent())) {
            throw new ApiException(ErrorCode.Business.ORDER_ENTRY_IMMUTABLE);
        }
    }

    private List<FinanceOrderLedgerEntry> targetEntries(FortuneFinanceOrderEntity order,
                                                        FortuneBillEntity oldBill,
                                                        FortuneBillEntity targetBill) {
        List<FortuneBillEntity> bills = new ArrayList<>(billRepo.getByOrderId(order.getOrderId()));
        if (oldBill != null) {
            bills.removeIf(bill -> Objects.equals(bill.getBillId(), oldBill.getBillId()));
        }
        bills.add(targetBill);
        return bills.stream().map(this::ledgerEntry).toList();
    }

    private FinanceOrderLedgerEntry ledgerEntry(FortuneBillEntity bill) {
        return new FinanceOrderLedgerEntry(bill.getBillId(), bill.getBillType(), bill.getOrderComponent(),
                bill.getAmount(), bill.getTradeTime(), Boolean.TRUE.equals(bill.getConfirm()),
                Boolean.TRUE.equals(bill.getDeleted()), Boolean.TRUE.equals(bill.getRecycleBin()),
                bill.getOrderLedgerVersion() == null ? 0 : bill.getOrderLedgerVersion());
    }

    private void validateAccounts(FortuneBookEntity book, FortuneFinanceOrderEntity order,
                                  Map<Long, BigDecimal> effects, boolean requireUsable) {
        effects.forEach((accountId, amount) ->
                accessGuard.validateTargetAccount(book, order, accountId, amount, requireUsable));
    }

    private boolean requiresUsableAccount(FinanceOrderEntryMutation.Operation operation) {
        return operation == FinanceOrderEntryMutation.Operation.ADD
                || operation == FinanceOrderEntryMutation.Operation.MODIFY
                || operation == FinanceOrderEntryMutation.Operation.CONFIRM
                || operation == FinanceOrderEntryMutation.Operation.RESTORE;
    }

    private void applyCashDeltas(Map<Long, BigDecimal> oldEffect, Map<Long, BigDecimal> newEffect) {
        mergeDeltas(oldEffect, newEffect, cashEffectCalculator)
                .forEach((accountId, delta) -> {
                    if (delta.compareTo(BigDecimal.ZERO) != 0) {
                        accountRepo.addBalanceAtomic(accountId, delta);
                    }
                });
    }

    private FortuneBillEntity persist(FinanceOrderEntryMutation command, FortuneBillEntity oldBill,
                                      FortuneBillEntity targetBill) {
        if (command.getOperation() == FinanceOrderEntryMutation.Operation.ADD) {
            return entryPersistence.add(command.getEntryCommand(), command.getFileList());
        }
        if (command.getOperation() == FinanceOrderEntryMutation.Operation.MODIFY) {
            entryPersistence.modify(targetBill, command.getEntryCommand(), command.getFileList());
            return targetBill;
        }
        entryPersistence.updateState(targetBill);
        return targetBill;
    }

    private void rebuildOrderSummary(FortuneFinanceOrderEntity order, FinanceOrderLedgerSnapshot snapshot,
                                     FinanceOrderEntryMutation.Operation operation, FortuneBillEntity bill) {
        if (operation == FinanceOrderEntryMutation.Operation.ADD
                && Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue())) {
            order.setStatus(FinanceOrderStatusEnum.USING.getValue());
            order.setSubmitTime(LocalDateTime.now(clock));
        }
        order.setOutAmount(snapshot.outAmount());
        order.setInAmount(snapshot.inAmount());
        order.setAdjustedAmount(snapshot.adjustedAmount());
        orderRepo.updateById(order);
    }

    private void rebuildSnapshots(FortuneBillEntity oldBill, FortuneBillEntity targetBill,
                                  Map<Long, BigDecimal> oldEffect, Map<Long, BigDecimal> newEffect) {
        if (oldEffect.equals(newEffect)) {
            return;
        }
        List<Long> accountIds = new ArrayList<>();
        accountIds.addAll(oldEffect.keySet());
        accountIds.addAll(newEffect.keySet());
        LocalDate anchorDate = LocalDate.now(clock);
        LocalDate startDate = anchorDate;
        if (oldBill != null && oldBill.getTradeTime() != null) {
            startDate = minDate(startDate, oldBill.getTradeTime().toLocalDate());
        }
        if (targetBill.getTradeTime() != null) {
            startDate = minDate(startDate, targetBill.getTradeTime().toLocalDate());
        }
        snapshotService.rebuildDailySnapshots(accountIds.stream().distinct().sorted().toList(), anchorDate, startDate);
    }

    private LocalDate minDate(LocalDate left, LocalDate right) {
        return left.isBefore(right) ? left : right;
    }

    private void saveAudit(FinanceOrderEntryMutation command, FortuneBillEntity oldBill,
                           FortuneBillEntity newBill, FinanceOrderLedgerSnapshot snapshot) {
        FortuneFinanceOrderAuditEntity audit = new FortuneFinanceOrderAuditEntity();
        audit.setBookId(command.getBookId());
        audit.setOrderId(command.getOrderId());
        audit.setBillId(newBill.getBillId());
        audit.setRequestId(command.getRequestId());
        audit.setAction(command.getOperation().name());
        audit.setBeforeJson(writeJson(auditSnapshot(oldBill)));
        audit.setAfterJson(writeJson(auditSnapshot(newBill, snapshot)));
        auditRepo.save(audit);
    }

    private Map<String, Object> auditSnapshot(FortuneBillEntity bill) {
        Map<String, Object> value = new LinkedHashMap<>();
        if (bill == null) {
            return value;
        }
        value.put("billId", bill.getBillId());
        value.put("accountId", bill.getAccountId());
        value.put("amount", bill.getAmount());
        value.put("confirm", bill.getConfirm());
        value.put("recycleBin", bill.getRecycleBin());
        value.put("orderComponent", bill.getOrderComponent());
        return value;
    }

    private Map<String, Object> auditSnapshot(FortuneBillEntity bill, FinanceOrderLedgerSnapshot snapshot) {
        Map<String, Object> value = auditSnapshot(bill);
        value.put("pendingAmount", snapshot.pendingAmount());
        value.put("settlementState", snapshot.settlementState());
        return value;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("单据审计JSON序列化失败", exception);
        }
    }

    private void saveRequestReceipt(Long bookId, String requestId, String requestHash,
                                    FinanceOrderMutationVo result) {
        FortuneFinanceOrderRequestEntity request = requestRepo.lambdaQuery()
                .eq(FortuneFinanceOrderRequestEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderRequestEntity::getRequestId, requestId)
                .one();
        request.setRequestHash(requestHash);
        request.setResponseJson(writeJson(result));
        requestRepo.updateById(request);
    }

    private void saveStateAudit(Long bookId, Long orderId, Long billId, String requestId, String action,
                                Map<String, Object> before, Map<String, Object> after) {
        FortuneFinanceOrderAuditEntity audit = new FortuneFinanceOrderAuditEntity();
        audit.setBookId(bookId);
        audit.setOrderId(orderId);
        audit.setBillId(billId);
        audit.setRequestId(requestId);
        audit.setAction(action);
        audit.setBeforeJson(writeJson(before));
        audit.setAfterJson(writeJson(after));
        auditRepo.save(audit);
    }
}
