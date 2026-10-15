package com.fortuneboot.service.fortune.order;

import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderRequestEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRequestRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单据写入审计与幂等回执记录器。
 * <p>统一占位请求、回执、状态审计和流水审计的持久化细节。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderMutationAuditRecorder {

    private final FortuneFinanceOrderRequestRepo requestRepo;
    private final FortuneFinanceOrderAuditRepo auditRepo;
    private final FinanceOrderAuditJsonSerializer auditJsonSerializer;

    public void insertRequestPlaceholder(Long bookId, String requestId, String operation, String requestHash) {
        FortuneFinanceOrderRequestEntity request = new FortuneFinanceOrderRequestEntity();
        request.setBookId(bookId);
        request.setRequestId(requestId);
        request.setOperation(operation);
        request.setRequestHash(requestHash);
        requestRepo.save(request);
    }

    public void saveRequestReceipt(Long bookId, String requestId, String requestHash,
                                   FinanceOrderMutationVo result) {
        FortuneFinanceOrderRequestEntity request = requestRepo.findByBookIdAndRequestId(bookId, requestId);
        request.setRequestHash(requestHash);
        request.setResponseJson(auditJsonSerializer.write(result));
        requestRepo.updateById(request);
    }

    public void saveStateAudit(Long bookId, Long orderId, Long billId, String requestId, String action,
                               Map<String, Object> before, Map<String, Object> after) {
        FortuneFinanceOrderAuditEntity audit = new FortuneFinanceOrderAuditEntity();
        audit.setBookId(bookId);
        audit.setOrderId(orderId);
        audit.setBillId(billId);
        audit.setRequestId(requestId);
        audit.setAction(action);
        audit.setBeforeJson(auditJsonSerializer.write(before));
        audit.setAfterJson(auditJsonSerializer.write(after));
        auditRepo.save(audit);
    }

    public void saveEntryAudit(FinanceOrderEntryMutation command, FortuneBillEntity oldBill,
                               FortuneBillEntity newBill, FinanceOrderLedgerSnapshot snapshot) {
        FortuneFinanceOrderAuditEntity audit = new FortuneFinanceOrderAuditEntity();
        audit.setBookId(command.getBookId());
        audit.setOrderId(command.getOrderId());
        audit.setBillId(newBill.getBillId());
        audit.setRequestId(command.getRequestId());
        audit.setAction(command.getOperation().name());
        audit.setBeforeJson(auditJsonSerializer.write(billAuditSnapshot(oldBill)));
        audit.setAfterJson(auditJsonSerializer.write(billAuditSnapshot(newBill, snapshot)));
        auditRepo.save(audit);
    }

    /**
     * 人工核对确认审计快照。金额是确认语义的核心证据，必须随前后状态落库。
     */
    public Map<String, Object> reconciliationAuditSnapshot(FortuneFinanceOrderEntity order) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", order.getOrderId());
        value.put("bookId", order.getBookId());
        value.put("currencyCode", order.getCurrencyCode());
        value.put("counterpartyName", order.getCounterpartyName());
        value.put("outAmount", order.getOutAmount());
        value.put("inAmount", order.getInAmount());
        value.put("adjustedAmount", order.getAdjustedAmount());
        value.put("version", order.getVersion());
        value.put("reconciliationStatus", order.getReconciliationStatus());
        value.put("reconciliationNote", order.getReconciliationNote());
        return value;
    }

    public Map<String, Object> orderAuditSnapshot(FortuneFinanceOrderEntity order) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", order.getOrderId());
        value.put("bookId", order.getBookId());
        value.put("title", order.getTitle());
        value.put("type", order.getType());
        value.put("status", order.getStatus());
        value.put("counterpartyId", order.getCounterpartyId());
        value.put("counterpartyName", order.getCounterpartyName());
        value.put("currencyCode", order.getCurrencyCode());
        value.put("dueDate", order.getDueDate() == null ? null : order.getDueDate().toString());
        value.put("remark", order.getRemark());
        value.put("version", order.getVersion());
        value.put("reconciliationStatus", order.getReconciliationStatus());
        value.put("closeTime", order.getCloseTime() == null ? null : order.getCloseTime().toString());
        return value;
    }

    private Map<String, Object> billAuditSnapshot(FortuneBillEntity bill) {
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

    private Map<String, Object> billAuditSnapshot(FortuneBillEntity bill, FinanceOrderLedgerSnapshot snapshot) {
        Map<String, Object> value = billAuditSnapshot(bill);
        value.put("pendingAmount", snapshot.pendingAmount());
        value.put("settlementState", snapshot.settlementState());
        return value;
    }
}
