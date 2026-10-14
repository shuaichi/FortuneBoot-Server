package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("单据生命周期写入器")
class FinanceOrderTransitionMutatorUnitTest {

    private final FortuneFinanceOrderRepo orderRepo = mock(FortuneFinanceOrderRepo.class);
    private final FortuneBillRepo billRepo = mock(FortuneBillRepo.class);
    private final FinanceOrderAccessGuard accessGuard = mock(FinanceOrderAccessGuard.class);
    private final FinanceOrderMutationAuditRecorder auditRecorder =
            mock(FinanceOrderMutationAuditRecorder.class);

    private FinanceOrderTransitionMutator mutator;

    @BeforeEach
    void setUp() {
        mutator = new FinanceOrderTransitionMutator(orderRepo, billRepo, accessGuard, auditRecorder,
                mock(FinanceOrderLedgerCalculator.class), mock(FinanceOrderLedgerEntryFactory.class),
                Clock.systemUTC());
    }

    @Test
    @DisplayName("已归档单据重复close返回幂等回执且不重新聚合流水")
    void closeClosedOrderIsIdempotentWithoutLedgerScan() {
        FinanceOrderTransitionCommand command = transitionCommand();
        FortuneFinanceOrderEntity order = closedOrder();

        FinanceOrderMutationVo result = mutator.transition(command, order, "hash");

        assertThat(result.getOrderId()).isEqualTo(order.getOrderId());
        assertThat(result.getBillId()).isNull();
        assertThat(result.getVersion()).isEqualTo(order.getVersion());
        assertThat(result.getReplayed()).isFalse();
        verify(auditRecorder).saveRequestReceipt(order.getBookId(), command.getRequestId(), "hash", result);
        verifyNoInteractions(orderRepo, billRepo);
    }

    private FinanceOrderTransitionCommand transitionCommand() {
        FinanceOrderTransitionCommand command = new FinanceOrderTransitionCommand();
        command.setBookId(9L);
        command.setOrderId(19L);
        command.setAction(FinanceOrderTransitionCommand.Action.CLOSE);
        command.setRequestId("request-1");
        command.setExpectedVersion(7L);
        return command;
    }

    private FortuneFinanceOrderEntity closedOrder() {
        FortuneFinanceOrderEntity order = new FortuneFinanceOrderEntity();
        order.setOrderId(19L);
        order.setBookId(9L);
        order.setStatus(FinanceOrderStatusEnum.CLOSE.getValue());
        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.READY.getValue());
        order.setVersion(7L);
        return order;
    }
}
