package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.fortune.FortuneGroupRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("相邻资源与单据历史引用保护")
class FinanceOrderReferenceGuardUnitTest {

    private final FortuneFinanceOrderRepo orderRepo = mock(FortuneFinanceOrderRepo.class);
    private final FortuneGroupRepo groupRepo = mock(FortuneGroupRepo.class);

    private FinanceOrderReferenceGuard guard;

    @BeforeEach
    void setUp() {
        guard = new FinanceOrderReferenceGuard(orderRepo, new FinanceOrderScopeLock(groupRepo));
    }

    @Test
    @DisplayName("账本存在任意单据历史时禁止删除或改默认币种")
    void bookWithOrderHistoryIsRejected() {
        when(orderRepo.existAnyByBookIdIncludeDeleted(9L)).thenReturn(true);

        assertThatThrownBy(() -> guard.ensureBookHasNoOrderHistory(9L))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.Business.ORDER_HISTORY_EXISTS));
    }

    @Test
    @DisplayName("无单据历史的账本允许删除并实际查询历史")
    void bookWithoutOrderHistoryPasses() {
        when(orderRepo.existAnyByBookIdIncludeDeleted(9L)).thenReturn(false);

        assertThatCode(() -> guard.ensureBookHasNoOrderHistory(9L)).doesNotThrowAnyException();
        verify(orderRepo).existAnyByBookIdIncludeDeleted(9L);
    }

    @Test
    @DisplayName("分组下任一账本存在单据历史即拒绝删除")
    void anyBookWithOrderHistoryIsRejected() {
        when(orderRepo.existAnyByBookIdsIncludeDeleted(List.of(9L, 10L))).thenReturn(true);

        assertThatThrownBy(() -> guard.ensureBooksHaveNoOrderHistory(List.of(9L, 10L)))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.Business.ORDER_HISTORY_EXISTS));
    }

    @Test
    @DisplayName("交易对象被单据历史引用时按已使用拒绝")
    void payeeReferencedByOrderIsRejected() {
        when(orderRepo.existAnyByCounterpartyIdIncludeDeleted(20L)).thenReturn(true);

        assertThatThrownBy(() -> guard.ensurePayeeHasNoOrderReference(20L))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.Business.PAYEE_ALREADY_USED));
    }

    @Test
    @DisplayName("分组锁获取失败时按分组不存在拒绝")
    void missingGroupLockIsRejected() {
        when(groupRepo.lockGroup(900L)).thenReturn(0);

        assertThatThrownBy(() -> guard.lockGroupScope(900L))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND));
    }
}
