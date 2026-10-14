package com.fortuneboot.service.fortune.order;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderLedgerVersionEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * 普通流水接口到单据管线的适配器。
 * <p>集中判断旧接口何时必须改走单据写入管线，并完成命令转换，避免普通流水服务理解单据细节。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Component
@RequiredArgsConstructor
public class FortuneBillOrderPipelineAdapter {

    private final FinanceOrderRequestService requestService;

    public boolean isOrderEntry(FortuneBillAddCommand command) {
        return Objects.nonNull(command.getOrderId())
                || BillTypeEnum.isOrderManagedValue(command.getBillType());
    }

    public boolean isOrderEntry(FortuneBillEntity bill, FortuneBillAddCommand command) {
        return Objects.nonNull(bill.getOrderId()) || Objects.nonNull(command.getOrderId())
                || BillTypeEnum.isOrderManagedValue(command.getBillType())
                || BillTypeEnum.isOrderManagedValue(bill.getBillType());
    }

    public boolean isManagedOrderBill(FortuneBillEntity bill) {
        return Objects.nonNull(bill.getOrderId())
                && FinanceOrderLedgerVersionEnum.isCurrentValue(bill.getOrderLedgerVersion());
    }

    public Long mutate(FinanceOrderEntryMutation.Operation operation, FortuneBillAddCommand command,
                       Long billId) {
        if (Objects.isNull(command.getOrderId())) {
            throw new ApiException(ErrorCode.Business.BILL_ORDER_ID_CAN_NOT_NULL);
        }
        FinanceOrderEntryCommand entryCommand = new FinanceOrderEntryCommand();
        BeanUtil.copyProperties(command, entryCommand);
        entryCommand.setBookId(command.getBookId());
        entryCommand.setOrderId(command.getOrderId());
        entryCommand.setRequestId(command.getRequestIdOrUuid());
        entryCommand.setExpectedVersion(command.getExpectedVersion());
        if (StringUtils.isBlank(entryCommand.getOrderComponent())
                && BillTypeEnum.isOrderManagedValue(entryCommand.getBillType())) {
            entryCommand.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        }
        FinanceOrderEntryMutation mutation = FinanceOrderEntryMutation.builder()
                .bookId(command.getBookId())
                .orderId(command.getOrderId())
                .billId(billId)
                .operation(operation)
                .entryCommand(entryCommand)
                .requestId(command.getRequestIdOrUuid())
                .expectedVersion(command.getExpectedVersion())
                .fileList(command.getFileList())
                .build();
        FinanceOrderMutationVo mutationVo = requestService.mutateEntry(mutation);
        return mutationVo.getBillId();
    }

    public void mutateState(FortuneBillEntity bill, FinanceOrderEntryMutation.Operation operation) {
        if (!isManagedOrderBill(bill)) {
            return;
        }
        FinanceOrderEntryMutation mutation = FinanceOrderEntryMutation.builder()
                .bookId(bill.getBookId())
                .orderId(bill.getOrderId())
                .billId(bill.getBillId())
                .operation(operation)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(null)
                .build();
        requestService.mutateEntry(mutation);
    }

    public void legacyInclude(Long bookId, Long billId, boolean include) {
        requestService.legacyInclude(bookId, billId, include);
    }
}
