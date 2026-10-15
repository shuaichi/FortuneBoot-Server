package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderReconciliationConfirmCommand;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.rule.fortune.order.FinanceOrderFieldRule;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.service.fortune.order.FinanceOrderAccessGuard;
import com.fortuneboot.service.fortune.order.FinanceOrderMutationAuditRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 历史单据人工确认写入器。
 * <p>用户确认的是报告中的金额语义；本组件只更新核对元数据与可选账本快照，绝不回放现金。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/15
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderManualConfirmMutator {

    private final FinanceOrderReconciliationScanner scanner;
    private final FortuneFinanceOrderRepo orderRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderMutationAuditRecorder auditRecorder;

    public FinanceOrderMutationVo confirm(FinanceOrderReconciliationConfirmCommand command,
                                          FortuneFinanceOrderEntity order, String requestHash) {
        if (!FinanceOrderReconciliationStatusEnum.isReviewRequiredValue(order.getReconciliationStatus())) {
            return confirmNoOp(command, order, requestHash);
        }

        FinanceOrderLedgerSnapshot snapshot = currentSnapshot(order);
        if (snapshot == null && order.getOutAmount() == null && order.getInAmount() == null) {
            throw new ApiException(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED);
        }

        long expectedVersion = Objects.requireNonNullElse(command.getExpectedVersion(), order.getVersion());
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        order.setVersion(expectedVersion + 1);
        Map<String, Object> before = auditRecorder.reconciliationAuditSnapshot(order);
        normalizeDisplayFields(order);
        applySnapshot(order, snapshot);

        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.READY.getValue());
        order.setReconciliationNote(null);
        orderRepo.updateById(order);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                order.getVersion(), false);
        auditRecorder.saveStateAudit(order.getBookId(), order.getOrderId(), null, command.getRequestId(),
                FinanceOrderAuditActionEnum.RECONCILE_CONFIRM.name(), before,
                auditRecorder.reconciliationAuditSnapshot(order));
        auditRecorder.saveRequestReceipt(order.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    private FinanceOrderLedgerSnapshot currentSnapshot(FortuneFinanceOrderEntity order) {
        return scanner.scan(order.getBookId()).orders().stream()
                .filter(item -> Objects.equals(item.getOrder().getOrderId(), order.getOrderId()))
                .findFirst()
                .map(FinanceOrderReconciliationOrderScan::getSnapshot)
                .orElse(null);
    }

    private void normalizeDisplayFields(FortuneFinanceOrderEntity order) {
        if (order.getCurrencyCode() == null) {
            FortuneBookEntity book = accessGuard.loadBook(order.getBookId());
            order.setCurrencyCode(book.getDefaultCurrency());
        }
        if (order.getCounterpartyName() == null || order.getCounterpartyName().isBlank()) {
            order.setCounterpartyName(FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME);
        }
    }

    private void applySnapshot(FortuneFinanceOrderEntity order, FinanceOrderLedgerSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        order.setOutAmount(snapshot.outAmount());
        order.setInAmount(snapshot.inAmount());
        order.setAdjustedAmount(snapshot.adjustedAmount());
    }

    private FinanceOrderMutationVo confirmNoOp(FinanceOrderReconciliationConfirmCommand command,
                                               FortuneFinanceOrderEntity order, String requestHash) {
        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                order.getVersion(), false);
        auditRecorder.saveRequestReceipt(order.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }
}
