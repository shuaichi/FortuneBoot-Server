package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderReconciliationConfirmCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderRequestEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRequestRepo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 单据幂等写入口。
 * <p>只负责请求规范化、幂等回放和并发竞态处理；哈希计算与命令执行分别由Hasher和MutationService承担。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderRequestService {

    private final FinanceOrderMutationService mutationService;
    private final FortuneFinanceOrderRequestRepo requestRepo;
    private final FinanceOrderRequestHasher requestHasher;

    public FinanceOrderMutationVo mutateEntry(FinanceOrderEntryMutation command) {
        normalizeRequestId(command);
        normalizeCommandForHash(command);
        String requestHash = requestHasher.hashEntry(command);
        return executeWithIdempotency(command.getBookId(), command.getRequestId(), requestHash,
                () -> mutationService.executeEntry(command, requestHash));
    }

    public FinanceOrderMutationVo create(FinanceOrderCreateCommand command) {
        normalizeCreateCommand(command);
        String requestHash = requestHasher.hashCreate(command);
        return executeWithIdempotency(command.getBookId(), command.getRequestId(), requestHash,
                () -> mutationService.executeCreate(command, requestHash));
    }

    public FinanceOrderMutationVo modify(FinanceOrderMetadataCommand command) {
        normalizeCreateCommand(command);
        String requestHash = requestHasher.hashModify(command);
        return executeWithIdempotency(command.getBookId(), command.getRequestId(), requestHash,
                () -> mutationService.executeModify(command, requestHash));
    }

    public FinanceOrderMutationVo transition(FinanceOrderTransitionCommand command) {
        normalizeRequestId(command);
        String requestHash = requestHasher.hashTransition(command.getAction().name(),
                command.getBookId(), command.getOrderId());
        return executeWithIdempotency(command.getBookId(), command.getRequestId(), requestHash,
                () -> mutationService.executeTransition(command, requestHash));
    }

    public FinanceOrderMutationVo confirmReconciliation(FinanceOrderReconciliationConfirmCommand command) {
        normalizeRequestId(command);
        String requestHash = requestHasher.hashReconciliationConfirm(command);
        return executeWithIdempotency(command.getBookId(), command.getRequestId(), requestHash,
                () -> mutationService.executeConfirmReconciliation(command, requestHash));
    }

    public FinanceOrderMutationVo legacyUsing(Long bookId, Long orderId) {
        String requestId = UUID.randomUUID().toString();
        String requestHash = requestHasher.hashTransition(FinanceOrderAuditActionEnum.USING.name(),
                bookId, orderId);
        return executeWithIdempotency(bookId, requestId, requestHash,
                () -> mutationService.executeLegacyUsing(bookId, orderId, requestId, requestHash));
    }

    public FinanceOrderMutationVo legacyInclude(Long bookId, Long billId, boolean include) {
        String requestId = UUID.randomUUID().toString();
        String requestHash = requestHasher.hashInclude(bookId, billId, include);
        return executeWithIdempotency(bookId, requestId, requestHash,
                () -> mutationService.executeInclude(bookId, billId, include, requestId, requestHash));
    }

    private FinanceOrderMutationVo executeWithIdempotency(Long bookId, String requestId, String requestHash,
                                                          Supplier<FinanceOrderMutationVo> execute) {
        FortuneFinanceOrderRequestEntity receipt = findReceipt(bookId, requestId);
        if (receipt != null) {
            return replay(receipt, requestHash);
        }
        try {
            return execute.get();
        } catch (DataAccessException exception) {
            FortuneFinanceOrderRequestEntity concurrentReceipt = findReceipt(bookId, requestId);
            if (concurrentReceipt != null) {
                return replay(concurrentReceipt, requestHash);
            }
            throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
        }
    }

    private FinanceOrderMutationVo replay(FortuneFinanceOrderRequestEntity receipt, String requestHash) {
        if (!Objects.equals(receipt.getRequestHash(), requestHash)) {
            throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
        }
        return readResponse(receipt.getResponseJson(), true);
    }

    private void normalizeCreateCommand(FinanceOrderCreateCommand command) {
        normalizeRequestId(command);
        command.setTitle(StringUtils.trimToEmpty(command.getTitle()));
        command.setCounterpartyName(StringUtils.trimToEmpty(command.getCounterpartyName()));
        command.setRemark(StringUtils.trimToEmpty(command.getRemark()));
    }

    private void normalizeRequestId(FinanceOrderCreateCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
    }

    private void normalizeRequestId(FinanceOrderTransitionCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
    }

    private void normalizeRequestId(FinanceOrderReconciliationConfirmCommand command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
    }

    private void normalizeRequestId(FinanceOrderEntryMutation command) {
        if (command.getRequestId() == null || command.getRequestId().isBlank()) {
            command.setRequestId(UUID.randomUUID().toString());
        }
        if (command.getEntryCommand() != null) {
            command.getEntryCommand().setRequestId(command.getRequestId());
        }
    }

    private void normalizeCommandForHash(FinanceOrderEntryMutation command) {
        FinanceOrderEntryCommand entryCommand = command.getEntryCommand();
        if (entryCommand == null || command.getBookId() == null || command.getOrderId() == null) {
            return;
        }
        entryCommand.setBookId(command.getBookId());
        entryCommand.setOrderId(command.getOrderId());
        FinanceOrderComponentEnum component =
                FinanceOrderComponentEnum.getByValue(entryCommand.getOrderComponent());
        if (component == FinanceOrderComponentEnum.PRINCIPAL
                && BillTypeEnum.isLoanValue(entryCommand.getBillType())) {
            entryCommand.setAmount(scale(entryCommand.getAmount()));
        } else if (entryCommand.getCategoryAmountPair() != null) {
            BigDecimal amount = entryCommand.getCategoryAmountPair().stream()
                    .map(CategoryAmountDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            entryCommand.setAmount(scale(amount));
        }
        entryCommand.setConvertedAmount(entryCommand.getAmount());
        entryCommand.setInclude(Objects.requireNonNullElse(entryCommand.getInclude(), Boolean.TRUE));
    }

    private BigDecimal scale(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private FortuneFinanceOrderRequestEntity findReceipt(Long bookId, String requestId) {
        return requestRepo.findByBookIdAndRequestId(bookId, requestId);
    }

    private FinanceOrderMutationVo readResponse(String responseJson, boolean replayed) {
        try {
            FinanceOrderMutationVo result = JacksonUtil.from(responseJson, FinanceOrderMutationVo.class);
            result.setReplayed(replayed);
            return result;
        } catch (Exception exception) {
            throw new ApiException(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
        }
    }
}
