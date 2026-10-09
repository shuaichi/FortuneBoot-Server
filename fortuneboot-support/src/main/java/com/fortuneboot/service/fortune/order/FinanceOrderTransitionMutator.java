package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 单据生命周期写入器。
 * <p>负责使用中、归档、重开与删除；调用方负责占位请求、锁和事务边界。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderTransitionMutator {

    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderMutationAuditRecorder auditRecorder;
    private final FinanceOrderLedgerCalculator ledgerCalculator;
    private final FinanceOrderLedgerEntryFactory ledgerEntryFactory;
    private final Clock clock;

    public FinanceOrderMutationVo transition(FinanceOrderTransitionCommand command,
                                             FortuneFinanceOrderEntity order, String requestHash) {
        return switch (command.getAction()) {
            case CLOSE -> closeOrder(command, order, requestHash);
            case REOPEN -> reopenOrder(command, order, requestHash);
            case REMOVE -> removeOrder(command, order, requestHash);
        };
    }

    public FinanceOrderMutationVo legacyUsing(Long bookId, Long orderId, String requestId,
                                             String requestHash, FortuneFinanceOrderEntity order) {
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_USING_STATUS_ERROR);
        }
        accessGuard.checkReconciliationReady(order);
        long expectedVersion = order.getVersion();
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        Map<String, Object> before = auditRecorder.orderAuditSnapshot(order);
        order.setStatus(FinanceOrderStatusEnum.USING.getValue());
        orderRepo.updateStatus(order.getOrderId(), FinanceOrderStatusEnum.USING);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                expectedVersion + 1, false);
        auditRecorder.saveStateAudit(bookId, orderId, null, requestId, FinanceOrderAuditActionEnum.USING.name(),
                before, auditRecorder.orderAuditSnapshot(order));
        auditRecorder.saveRequestReceipt(bookId, requestId, requestHash, result);
        return result;
    }

    private FinanceOrderMutationVo closeOrder(FinanceOrderTransitionCommand command,
                                              FortuneFinanceOrderEntity order, String requestHash) {
        accessGuard.checkReconciliationReady(order);
        if (Objects.equals(order.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue())) {
            return noOpTransition(command, order, requestHash);
        }
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_CLOSE_STATUS_ERROR);
        }
        FinanceOrderLedgerSnapshot snapshot = ledgerCalculator.calculate(order.getType(),
                billRepo.getByOrderId(order.getOrderId()).stream()
                        .map(ledgerEntryFactory::from)
                        .toList());
        if (snapshot.pendingEntryCount() > 0) {
            throw new ApiException(ErrorCode.Business.ORDER_HAS_PENDING_ENTRIES);
        }
        if (snapshot.issuedAmount().compareTo(BigDecimal.ZERO) <= 0
                || snapshot.pendingAmount().compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException(ErrorCode.Business.ORDER_NOT_SETTLED,
                    snapshot.settledAmount().add(snapshot.adjustedAmount()).toPlainString(),
                    snapshot.issuedAmount().toPlainString());
        }

        long expectedVersion = Objects.requireNonNullElse(command.getExpectedVersion(), order.getVersion());
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        Map<String, Object> before = auditRecorder.orderAuditSnapshot(order);
        LocalDateTime closeTime = LocalDateTime.now(clock);
        order.setStatus(FinanceOrderStatusEnum.CLOSE.getValue());
        order.setCloseTime(closeTime);
        orderRepo.updateStatusAndCloseTime(order.getOrderId(), FinanceOrderStatusEnum.CLOSE, closeTime);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                expectedVersion + 1, false);
        auditRecorder.saveStateAudit(order.getBookId(), order.getOrderId(), null, command.getRequestId(),
                FinanceOrderAuditActionEnum.CLOSE.name(),
                before, auditRecorder.orderAuditSnapshot(order));
        auditRecorder.saveRequestReceipt(order.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    private FinanceOrderMutationVo reopenOrder(FinanceOrderTransitionCommand command,
                                               FortuneFinanceOrderEntity order, String requestHash) {
        accessGuard.checkReconciliationReady(order);
        if (Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            return noOpTransition(command, order, requestHash);
        }
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_REOPEN_STATUS_ERROR);
        }

        long expectedVersion = Objects.requireNonNullElse(command.getExpectedVersion(), order.getVersion());
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        Map<String, Object> before = auditRecorder.orderAuditSnapshot(order);
        order.setStatus(FinanceOrderStatusEnum.USING.getValue());
        order.setCloseTime(null);
        orderRepo.updateStatusAndCloseTime(order.getOrderId(), FinanceOrderStatusEnum.USING, null);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                expectedVersion + 1, false);
        auditRecorder.saveStateAudit(order.getBookId(), order.getOrderId(), null, command.getRequestId(),
                FinanceOrderAuditActionEnum.REOPEN.name(),
                before, auditRecorder.orderAuditSnapshot(order));
        auditRecorder.saveRequestReceipt(order.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    private FinanceOrderMutationVo removeOrder(FinanceOrderTransitionCommand command,
                                               FortuneFinanceOrderEntity order, String requestHash) {
        accessGuard.checkReconciliationReady(order);
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue())
                && !Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_ENTRY_IMMUTABLE);
        }
        if (billRepo.existAnyByOrderIdIncludeDeleted(order.getOrderId())) {
            throw new ApiException(ErrorCode.Business.ORDER_HISTORY_EXISTS);
        }

        long expectedVersion = Objects.requireNonNullElse(command.getExpectedVersion(), order.getVersion());
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        Map<String, Object> before = auditRecorder.orderAuditSnapshot(order);
        orderRepo.removeById(order.getOrderId());

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                expectedVersion + 1, false);
        auditRecorder.saveStateAudit(order.getBookId(), order.getOrderId(), null, command.getRequestId(),
                FinanceOrderAuditActionEnum.REMOVE.name(),
                before, Map.of("deleted", true));
        auditRecorder.saveRequestReceipt(order.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    private FinanceOrderMutationVo noOpTransition(FinanceOrderTransitionCommand command,
                                                  FortuneFinanceOrderEntity order, String requestHash) {
        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                order.getVersion(), false);
        auditRecorder.saveRequestReceipt(order.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }
}
