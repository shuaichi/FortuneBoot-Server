package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.rule.fortune.order.FinanceOrderFieldRule;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 单据元数据写入器。
 * <p>负责新建与修改单据元数据；调用方负责占位请求、锁和事务边界。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderMetadataMutator {

    private final FortuneFinanceOrderRepo orderRepo;
    private final FortuneBillRepo billRepo;
    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderMutationAuditRecorder auditRecorder;
    private final Clock clock;

    public FinanceOrderMutationVo create(FinanceOrderCreateCommand command, String requestHash) {
        FortuneBookEntity book = accessGuard.loadBook(command.getBookId());
        accessGuard.validateOrderType(command.getType());
        String title = normalizeTitle(command.getTitle());
        String counterpartyName = accessGuard.resolveCounterpartyName(command.getBookId(),
                command.getCounterpartyId(), command.getCounterpartyName());
        String remark = normalizeRemark(command.getRemark());

        FortuneFinanceOrderEntity order = new FortuneFinanceOrderEntity();
        order.setBookId(book.getBookId());
        order.setTitle(title);
        order.setType(command.getType());
        order.setOutAmount(BigDecimal.ZERO);
        order.setInAmount(BigDecimal.ZERO);
        order.setAdjustedAmount(BigDecimal.ZERO);
        order.setStatus(FinanceOrderStatusEnum.INIT.getValue());
        order.setSubmitTime(LocalDateTime.now(clock));
        order.setRemark(remark);
        order.setCounterpartyId(command.getCounterpartyId());
        order.setCounterpartyName(counterpartyName);
        order.setCurrencyCode(book.getDefaultCurrency());
        order.setDueDate(command.getDueDate());
        order.setVersion(0L);
        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.READY.getValue());
        orderRepo.save(order);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null, 0L, false);
        auditRecorder.saveStateAudit(order.getBookId(), order.getOrderId(), null, command.getRequestId(),
                FinanceOrderAuditActionEnum.CREATE.name(),
                Map.of(), auditRecorder.orderAuditSnapshot(order));
        auditRecorder.saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    public FinanceOrderMutationVo modify(FinanceOrderMetadataCommand command, String requestHash) {
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(command.getBookId(), command.getOrderId());
        accessGuard.checkReconciliationReady(order);
        if (Objects.equals(order.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_CLOSE_CAN_NOT_MODIFY);
        }
        if (!Objects.equals(order.getType(), command.getType())) {
            throw new ApiException(ErrorCode.Business.ORDER_TYPE_MISMATCH);
        }
        String title = normalizeTitle(command.getTitle());
        String counterpartyName = accessGuard.resolveCounterpartyName(command.getBookId(),
                command.getCounterpartyId(), command.getCounterpartyName());
        String remark = normalizeRemark(command.getRemark());
        boolean counterpartyChanged = !Objects.equals(order.getCounterpartyId(), command.getCounterpartyId())
                || !Objects.equals(legacyCounterpartyName(order), counterpartyName);
        if (counterpartyChanged && billRepo.existAnyByOrderIdIncludeDeleted(order.getOrderId())) {
            throw new ApiException(ErrorCode.Business.ORDER_HISTORY_EXISTS);
        }

        boolean changed = !Objects.equals(order.getTitle(), title)
                || !Objects.equals(StringUtils.defaultString(order.getRemark()), remark)
                || !Objects.equals(order.getDueDate(), command.getDueDate())
                || counterpartyChanged;
        if (!changed) {
            FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                    order.getVersion(), false);
            auditRecorder.saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
            return result;
        }

        long expectedVersion = Objects.requireNonNullElse(command.getExpectedVersion(), order.getVersion());
        if (orderRepo.compareAndIncrementVersion(order.getOrderId(), order.getBookId(), expectedVersion) != 1) {
            throw new ApiException(ErrorCode.Business.ORDER_VERSION_CONFLICT);
        }
        Map<String, Object> before = auditRecorder.orderAuditSnapshot(order);
        order.setTitle(title);
        order.setCounterpartyId(command.getCounterpartyId());
        order.setCounterpartyName(counterpartyName);
        order.setDueDate(command.getDueDate());
        order.setRemark(remark);
        orderRepo.updateMetadata(order.getOrderId(), title, command.getCounterpartyId(),
                counterpartyName, command.getDueDate(), remark);

        FinanceOrderMutationVo result = new FinanceOrderMutationVo(order.getOrderId(), null,
                expectedVersion + 1, false);
        auditRecorder.saveStateAudit(order.getBookId(), order.getOrderId(), null, command.getRequestId(),
                FinanceOrderAuditActionEnum.MODIFY.name(),
                before, auditRecorder.orderAuditSnapshot(order));
        auditRecorder.saveRequestReceipt(command.getBookId(), command.getRequestId(), requestHash, result);
        return result;
    }

    private String normalizeTitle(String title) {
        String normalized = StringUtils.trimToEmpty(title);
        if (!FinanceOrderFieldRule.isValidTitle(normalized)) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "标题必须为1到50个字符");
        }
        return normalized;
    }

    private String normalizeRemark(String remark) {
        String normalized = StringUtils.trimToEmpty(remark);
        if (!FinanceOrderFieldRule.isValidRemark(normalized)) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "备注长度不能超过512个字符");
        }
        return normalized;
    }

    private String legacyCounterpartyName(FortuneFinanceOrderEntity order) {
        return StringUtils.defaultIfBlank(order.getCounterpartyName(), FinanceOrderFieldRule.DEFAULT_COUNTERPARTY_NAME);
    }
}
