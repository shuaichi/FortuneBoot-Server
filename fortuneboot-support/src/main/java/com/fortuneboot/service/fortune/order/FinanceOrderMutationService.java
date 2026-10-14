package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderAuditActionEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderMetadataCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单据写事务编排服务。
 * <p>只负责请求占位、分组锁、事务边界和写入器编排；具体业务规则由AccessGuard与各Mutator承担。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@RequiredArgsConstructor
public class FinanceOrderMutationService {

    private final FinanceOrderAccessGuard accessGuard;
    private final FinanceOrderScopeLock scopeLock;
    private final FinanceOrderMutationAuditRecorder auditRecorder;
    private final FinanceOrderMetadataMutator metadataMutator;
    private final FinanceOrderTransitionMutator transitionMutator;
    private final FinanceOrderEntryMutator entryMutator;

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeCreate(FinanceOrderCreateCommand command, String requestHash) {
        auditRecorder.insertRequestPlaceholder(command.getBookId(), command.getRequestId(),
                FinanceOrderAuditActionEnum.CREATE.name(), requestHash);
        lockBookGroup(command.getBookId());
        return metadataMutator.create(command, requestHash);
    }

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeModify(FinanceOrderMetadataCommand command, String requestHash) {
        auditRecorder.insertRequestPlaceholder(command.getBookId(), command.getRequestId(),
                FinanceOrderAuditActionEnum.MODIFY.name(), requestHash);
        lockBookGroup(command.getBookId());
        return metadataMutator.modify(command, requestHash);
    }

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeTransition(FinanceOrderTransitionCommand command, String requestHash) {
        auditRecorder.insertRequestPlaceholder(command.getBookId(), command.getRequestId(),
                command.getAction().name(), requestHash);
        lockBookGroup(command.getBookId());
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(command.getBookId(), command.getOrderId());
        return transitionMutator.transition(command, order, requestHash);
    }

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeLegacyUsing(Long bookId, Long orderId, String requestId,
                                                     String requestHash) {
        auditRecorder.insertRequestPlaceholder(bookId, requestId,
                FinanceOrderAuditActionEnum.USING.name(), requestHash);
        lockBookGroup(bookId);
        FortuneFinanceOrderEntity order = accessGuard.loadOrder(bookId, orderId);
        return transitionMutator.legacyUsing(bookId, orderId, requestId, requestHash, order);
    }

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeEntry(FinanceOrderEntryMutation command, String requestHash) {
        auditRecorder.insertRequestPlaceholder(command.getBookId(), command.getRequestId(),
                command.getOperation().name(), requestHash);
        lockBookGroup(command.getBookId());
        FortuneBookEntity book = accessGuard.loadBook(command.getBookId());
        return entryMutator.mutate(book, command, requestHash);
    }

    @Transactional(rollbackFor = Exception.class)
    public FinanceOrderMutationVo executeInclude(Long bookId, Long billId, boolean include,
                                                 String requestId, String requestHash) {
        auditRecorder.insertRequestPlaceholder(bookId, requestId,
                include ? FinanceOrderAuditActionEnum.INCLUDE.name()
                        : FinanceOrderAuditActionEnum.EXCLUDE.name(),
                requestHash);
        FortuneBillEntity billBeforeLock = entryMutator.loadOrderBoundBill(billId);
        lockBookGroup(bookId);
        FortuneBookEntity book = accessGuard.loadBook(bookId);
        return entryMutator.include(book, billBeforeLock, include, requestId, requestHash);
    }

    private void lockBookGroup(Long bookId) {
        FortuneBookEntity bookBeforeLock = accessGuard.loadBook(bookId);
        scopeLock.lockGroup(bookBeforeLock.getGroupId());
    }
}
