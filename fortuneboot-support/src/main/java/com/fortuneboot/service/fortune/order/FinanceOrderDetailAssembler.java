package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderEntryActionEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderReconciliationStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.domain.dto.fortune.order.FinanceOrderLedgerSnapshot;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 单据详情装配器。
 * <p>只负责实体、快照到VO的转换和单据级动作权限计算，不访问数据库。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FinanceOrderDetailAssembler {

    private final Clock clock;

    public FinanceOrderDetailVo build(FortuneFinanceOrderEntity order,
                                       FinanceOrderLedgerSnapshot snapshot,
                                       Boolean hasBills,
                                       boolean actor) {
        boolean ready = FinanceOrderReconciliationStatusEnum.isReadyValue(order.getReconciliationStatus());
        FinanceOrderStatusEnum status = FinanceOrderStatusEnum.getByValue(order.getStatus());
        FinanceOrderTypeEnum orderType = FinanceOrderTypeEnum.getByValue(order.getType());

        FinanceOrderDetailVo vo = new FinanceOrderDetailVo();
        vo.setOrderId(order.getOrderId());
        vo.setBookId(order.getBookId());
        vo.setTitle(order.getTitle());
        vo.setType(order.getType());
        vo.setTypeDesc(FinanceOrderTypeEnum.getDescByValue(order.getType()));
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(FinanceOrderStatusEnum.getDescByValue(order.getStatus()));
        vo.setCounterpartyId(order.getCounterpartyId());
        vo.setCounterpartyName(order.getCounterpartyName());
        vo.setCurrencyCode(order.getCurrencyCode());
        vo.setDueDate(order.getDueDate());
        vo.setSubmitTime(order.getSubmitTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setRemark(order.getRemark());
        vo.setVersion(order.getVersion());
        vo.setReconciliationStatus(order.getReconciliationStatus());
        vo.setReconciliationNote(order.getReconciliationNote());

        if (ready && snapshot != null) {
            vo.setOutAmount(FinanceOrderAmountRule.format(snapshot.outAmount()));
            vo.setInAmount(FinanceOrderAmountRule.format(snapshot.inAmount()));
            vo.setIssuedAmount(FinanceOrderAmountRule.format(snapshot.issuedAmount()));
            vo.setSettledAmount(FinanceOrderAmountRule.format(snapshot.settledAmount()));
            vo.setAdjustedAmount(FinanceOrderAmountRule.format(snapshot.adjustedAmount()));
            vo.setPendingAmount(FinanceOrderAmountRule.format(snapshot.pendingAmount()));
            vo.setInterestIncome(FinanceOrderAmountRule.format(snapshot.interestIncome()));
            vo.setInterestExpense(FinanceOrderAmountRule.format(snapshot.interestExpense()));
            vo.setFeeAmount(FinanceOrderAmountRule.format(snapshot.feeAmount()));
            vo.setNetCostOrIncome(FinanceOrderAmountRule.format(snapshot.netCostOrIncome()));
            vo.setSettlementState(snapshot.settlementState());
            vo.setPendingEntryCount(snapshot.pendingEntryCount());
        } else {
            vo.setSettlementState(FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue());
            vo.setReadOnlyReason(StringUtils.defaultIfBlank(order.getReconciliationNote(),
                    FinanceOrderReconciliationStatusEnum.REVIEW_REQUIRED.getValue()));
        }

        boolean editableStatus = status != null && status.isEditable();
        boolean pendingZero = snapshot == null || snapshot.pendingAmount().compareTo(BigDecimal.ZERO) == 0;
        boolean issued = snapshot != null && snapshot.issuedAmount().compareTo(BigDecimal.ZERO) > 0;
        vo.setCanEdit(actor && ready && editableStatus);
        vo.setCanDelete(actor && ready && editableStatus && !Boolean.TRUE.equals(hasBills));
        vo.setCanClose(actor && ready && status != null && status.isUsing()
                && issued && pendingZero && (snapshot == null || snapshot.pendingEntryCount() == 0));
        vo.setCanReopen(actor && ready && status != null && status.isClosed());
        vo.setAllowedEntryActions(allowedEntryActions(orderType, status, actor, ready));
        vo.setOverdue(order.getDueDate() != null
                && order.getDueDate().isBefore(LocalDate.now(clock))
                && snapshot != null
                && snapshot.pendingAmount().compareTo(BigDecimal.ZERO) > 0);
        return vo;
    }

    private String[] allowedEntryActions(FinanceOrderTypeEnum orderType, FinanceOrderStatusEnum status,
                                         boolean actor, boolean ready) {
        if (!actor || !ready || orderType == null || status == null) {
            return new String[0];
        }
        if (status.isInitial()) {
            return toArray(orderType.initialEntryActions());
        }
        if (!status.isUsing()) {
            return new String[0];
        }
        return toArray(orderType.usingEntryActions());
    }

    private String[] toArray(List<FinanceOrderEntryActionEnum> actions) {
        return actions.stream()
                .map(FinanceOrderEntryActionEnum::name)
                .toArray(String[]::new);
    }
}
