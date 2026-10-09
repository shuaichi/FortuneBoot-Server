package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.repository.fortune.FortuneGroupRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FinanceOrderScopeLock {

    private final FortuneGroupRepo fortuneGroupRepo;

    public void lockGroup(Long groupId) {
        if (groupId == null || fortuneGroupRepo.lockGroup(groupId) != 1) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, groupId, "分组");
        }
    }
}
