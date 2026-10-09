package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 相邻资源与单据历史的引用保护。
 * <p>统一账本、分组、交易对象在删除或变更前需要的单据历史校验和分组锁，避免基础资源服务理解单据内部实现。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderReferenceGuard {

    private final FortuneFinanceOrderRepo orderRepo;
    private final FinanceOrderScopeLock scopeLock;

    /**
     * 获取分组级单据写入锁，保证删除相邻资源时不与单据写入交错。
     */
    public void lockGroupScope(Long groupId) {
        scopeLock.lockGroup(groupId);
    }

    /**
     * 账本存在任意单据历史（含逻辑删除）时禁止删除或修改默认币种。
     */
    public void ensureBookHasNoOrderHistory(Long bookId) {
        if (orderRepo.existAnyByBookIdIncludeDeleted(bookId)) {
            throw new ApiException(ErrorCode.Business.ORDER_HISTORY_EXISTS);
        }
    }

    /**
     * 分组下任一账本存在单据历史时禁止删除分组。
     */
    public void ensureBooksHaveNoOrderHistory(Collection<Long> bookIds) {
        if (orderRepo.existAnyByBookIdsIncludeDeleted(bookIds)) {
            throw new ApiException(ErrorCode.Business.ORDER_HISTORY_EXISTS);
        }
    }

    /**
     * 交易对象被任意单据历史引用时禁止删除。
     */
    public void ensurePayeeHasNoOrderReference(Long payeeId) {
        if (orderRepo.existAnyByCounterpartyIdIncludeDeleted(payeeId)) {
            throw new ApiException(ErrorCode.Business.PAYEE_ALREADY_USED);
        }
    }
}
