package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerEntry;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.service.fortune.FortuneBalanceSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 单据流水写入器。
 * <p>负责流水新增、修改、状态动作、现金余额重建和包含关系切换；调用方负责占位请求、锁和事务边界。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderEntryMutator {

    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FortuneAccountRepo accountRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderMutationAuditRecorder auditRecorder;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final FinanceOrderCashEffectCalculator cashEffectCalculator;
    private final FinanceOrderEntryPersistence entryPersistence;
    private final FortuneBalanceSnapshotService snapshotService;
    private final FinanceOrderBillFactory billFactory;
    private final FinanceOrderLedgerEntryFactory ledgerEntryFactory;
    private final Clock clock;

    public FortuneBillEntity loadOrderBoundBill(Long billId) {
        FortuneBillEntity bill = billRepo.getById(billId);
        if (bill == null || bill.getOrderId() == null) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, billId, "流水");
        }
        return bill;
    }

    public FinanceOrderMutationVo mutate(FortuneBookEntity book, FinanceOrderEntryMutation command,
                                         String requestHash) {
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
            auditRecorder.saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
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

        FortuneBillEntity targetBill = buildTargetBill(order, oldBill, command);
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
        auditRecorder.saveEntryAudit(command, oldBill, persistedBill, targetSnapshot);
        auditRecorder.saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    public FinanceOrderMutationVo include(FortuneBookEntity book, FortuneBillEntity billBeforeLock,
                                          boolean include, String requestId, String requestHash) {
        FortuneFinanceOrderEntity order = accessGuard.loadMutableOrder(book, billBeforeLock.getOrderId(), false);
        FortuneBillEntity oldBill = accessGuard.loadBill(order, billBeforeLock.getBillId());
        if (Objects.equals(oldBill.getInclude(), include)) {
            FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), oldBill.getBillId(),
                    order.getVersion(), true);
            auditRecorder.saveRequestReceipt(book.getBookId(), requestId, requestHash, result);
            return result;
        }

        long expectedVersion = order.getVersion();
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), book.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        order = accessGuard.loadMutableOrder(book, order.getOrderId(), true);
        oldBill.setInclude(include);
        entryPersistence.updateState(oldBill);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), oldBill.getBillId(),
                expectedVersion + 1, false);
        auditRecorder.saveStateAudit(book.getBookId(), order.getOrderId(), oldBill.getBillId(), requestId,
                include ? FinanceOrderAuditActionEnum.INCLUDE.name()
                        : FinanceOrderAuditActionEnum.EXCLUDE.name(),
                Map.of("include", !include), Map.of("include", include));
        auditRecorder.saveRequestReceipt(book.getBookId(), requestId, requestHash, result);
        return result;
    }

    static Map<Long, BigDecimal> mergeDeltas(Map<Long, BigDecimal> oldEffect, Map<Long, BigDecimal> newEffect) {
        Map<Long, BigDecimal> deltas = new TreeMap<>();
        oldEffect.forEach((accountId, amount) -> deltas.merge(accountId, amount.negate(), BigDecimal::add));
        newEffect.forEach((accountId, amount) -> deltas.merge(accountId, amount, BigDecimal::add));
        return deltas;
    }

    private FortuneBillEntity buildTargetBill(FortuneFinanceOrderEntity order, FortuneBillEntity oldBill,
                                              FinanceOrderEntryMutation command) {
        if (command.getOperation() == FinanceOrderEntryMutation.Operation.ADD) {
            FinanceOrderEntryCommand normalized = accessGuard.normalizeCommand(order, command.getEntryCommand());
            return billFactory.buildAddBill(normalized);
        }
        if (command.getOperation() == FinanceOrderEntryMutation.Operation.MODIFY) {
            FinanceOrderEntryCommand normalized = accessGuard.normalizeCommand(order, command.getEntryCommand());
            validateImmutableFields(oldBill, normalized);
            FortuneBillEntity targetBill = billFactory.copyBill(oldBill);
            billFactory.applyModify(targetBill, normalized);
            return targetBill;
        }
        FortuneBillEntity targetBill = billFactory.copyBill(oldBill);
        applyState(targetBill, command.getOperation());
        return targetBill;
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
        return bills.stream().map(ledgerEntryFactory::from).toList();
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
        mergeDeltas(oldEffect, newEffect)
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
}
