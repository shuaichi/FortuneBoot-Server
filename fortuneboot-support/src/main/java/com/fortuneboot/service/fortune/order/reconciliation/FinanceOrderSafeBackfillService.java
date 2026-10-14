package com.fortuneboot.service.fortune.order.reconciliation;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.rule.fortune.order.FinanceOrderFieldRule;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.service.fortune.order.FinanceOrderAccessGuard;
import com.fortuneboot.service.fortune.order.FinanceOrderAuditJsonSerializer;
import com.fortuneboot.service.fortune.order.FinanceOrderScopeLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 历史单据安全回填服务。
 * <p>只在可证明安全的场景重建缓存与组件标注，不回放现金。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderSafeBackfillService {

    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FortuneFinanceOrderAuditRepo auditRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderScopeLock scopeLock;
    private final FinanceOrderAuditJsonSerializer auditJsonSerializer;

    public void backfill(FinanceOrderReconciliationOrderScan orderScan) {
        FortuneFinanceOrderEntity before = orderScan.getOrder();
        scopeLock.lockGroup(before.getBookId() == null
                ? null : accessGuard.loadBook(before.getBookId()).getGroupId());
        FortuneFinanceOrderEntity order = orderRepo.getById(before.getOrderId());
        if (order == null || !Objects.equals(order.getBookId(), before.getBookId())
                || !FinanceOrderReconciliationStatusEnum.isReviewRequiredValue(order.getReconciliationStatus())) {
            return;
        }
        long expectedVersion = order.getVersion();
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        order.setVersion(expectedVersion + 1);
        Map<String, Object> beforeJson = auditSnapshot(order);

        if (order.getCurrencyCode() == null) {
            order.setCurrencyCode(accessGuard.loadBook(order.getBookId()).getDefaultCurrency());
        }
        if (order.getCounterpartyName() == null || order.getCounterpartyName().isBlank()) {
            order.setCounterpartyName(FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME);
        }
        FinanceOrderLedgerSnapshot snapshot = orderScan.getSnapshot();
        if (snapshot != null) {
            order.setOutAmount(snapshot.outAmount());
            order.setInAmount(snapshot.inAmount());
            order.setAdjustedAmount(snapshot.adjustedAmount());
            for (FortuneBillEntity bill : orderScan.getBills()) {
                if (FinanceOrderLedgerVersionEnum.isLegacyValue(bill.getOrderLedgerVersion())
                        && BillTypeEnum.isClaimValue(bill.getBillType())) {
                    bill.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
                    bill.setOrderLedgerVersion(FinanceOrderLedgerVersionEnum.CURRENT.getValue());
                    billRepo.updateById(bill);
                }
            }
        }
        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.READY.getValue());
        order.setReconciliationNote(null);
        orderRepo.updateById(order);

        FortuneFinanceOrderAuditEntity audit = new FortuneFinanceOrderAuditEntity();
        audit.setBookId(order.getBookId());
        audit.setOrderId(order.getOrderId());
        audit.setAction(FinanceOrderAuditActionEnum.RECONCILE.name());
        audit.setBeforeJson(auditJsonSerializer.write(beforeJson));
        audit.setAfterJson(auditJsonSerializer.write(auditSnapshot(order)));
        auditRepo.save(audit);
    }

    private Map<String, Object> auditSnapshot(FortuneFinanceOrderEntity order) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", order.getOrderId());
        value.put("status", order.getStatus());
        value.put("currencyCode", order.getCurrencyCode());
        value.put("counterpartyName", order.getCounterpartyName());
        value.put("outAmount", order.getOutAmount());
        value.put("inAmount", order.getInAmount());
        value.put("adjustedAmount", order.getAdjustedAmount());
        value.put("version", order.getVersion());
        value.put("reconciliationStatus", order.getReconciliationStatus());
        return value;
    }
}
