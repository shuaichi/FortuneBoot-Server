package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import com.fortuneboot.service.fortune.FortuneBillExtraCategoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("单据访问保护规则")
class FinanceOrderAccessGuardUnitTest {

    private FinanceOrderAccessGuard guard;

    @BeforeEach
    void setUp() {
        guard = new FinanceOrderAccessGuard(
                mock(FortuneBookRepo.class),
                mock(FortuneFinanceOrderRepo.class),
                mock(FortuneBillRepo.class),
                mock(FortuneAccountRepo.class),
                mock(FortuneCategoryRepo.class),
                mock(FortuneTagRepo.class),
                mock(FortuneMemberRepo.class),
                mock(FortunePayeeRepo.class),
                mock(FortuneBillExtraCategoryValidator.class));
    }

    @Test
    @DisplayName("核对就绪的单据允许写入")
    void reconciliationReadyOrderPasses() {
        FortuneFinanceOrderEntity order = new FortuneFinanceOrderEntity();
        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.READY.getValue());

        assertThatCode(() -> guard.checkReconciliationReady(order)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("待核对的单据统一拒绝写入")
    void reviewRequiredOrderIsRejected() {
        FortuneFinanceOrderEntity order = new FortuneFinanceOrderEntity();
        order.setReconciliationStatus(FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue());

        assertThatThrownBy(() -> guard.checkReconciliationReady(order))
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED);
    }
}
