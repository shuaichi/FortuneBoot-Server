package com.fortuneboot.service.fortune.importer;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.factory.fortune.factory.FortuneBillFactory;
import com.fortuneboot.service.fortune.FortuneBillExtraCategoryValidator;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.service.fortune.order.FinanceOrderRequestService;
import com.fortuneboot.strategy.bill.BillProcessStrategy;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import com.fortuneboot.strategy.bill.BillStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author zhangchi118
 */
@Component
@RequiredArgsConstructor
class FortuneBillImportRowPersistor {

    private final FortuneBillFactory fortuneBillFactory;
    private final BillStrategyFactory strategyFactory;
    private final FortuneBillImportStrategyContextBuilder strategyContextBuilder;
    private final FortuneBillImportNetAmountValidator netAmountValidator;
    private final FortuneBillExtraCategoryValidator extraCategoryValidator;
    private final FortuneBillImportRelationBatch relationBatch;
    private final FinanceOrderRequestService financeOrderRequestService;

    void persist(FortuneBillImportRow row, FortuneBillImportContext importContext,
                 FortuneBillImportRelationBatch.Relations relations, List<Long> billIds) {
        try {
            FortuneBillAddCommand command = row.getCommand();
            extraCategoryValidator.validate(command);
            if (Objects.equals(command.getBillType(), BillTypeEnum.ADVANCE.getValue())
                    || Objects.equals(command.getBillType(), BillTypeEnum.REIMBURSE.getValue())) {
                persistThroughOrderPipeline(row, command, billIds);
                return;
            }
            FortuneBillModel billModel = fortuneBillFactory.create();
            billModel.loadAddCommand(command);
            BillStrategyContext context = strategyContextBuilder.build(command, billModel, importContext);
            BillProcessStrategy strategy = strategyFactory.getStrategy(command.getBillType());
            if (Objects.isNull(strategy)) {
                throw new ApiException(ErrorCode.Business.BILL_TYPE_ILLEGAL, command.getBillType());
            }
            strategy.convertRate(context);
            netAmountValidator.validate(command, billModel);
            strategy.confirmBalance(context);
            strategy.operateFinanceOrder(context);
            billModel.insert();
            row.setBillId(billModel.getBillId());
            billIds.add(billModel.getBillId());
            relationBatch.collect(relations, command, billModel.getBillId());
        } catch (ApiException e) {
            row.addError("第" + row.getRowNum() + "行：" + e.getMessage());
            throw e;
        }
    }

    private void persistThroughOrderPipeline(FortuneBillImportRow row, FortuneBillAddCommand source,
                                             List<Long> billIds) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        BeanUtil.copyProperties(source, command);
        command.setOrderId(source.getOrderId());
        command.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(null);
        command.setFileList(List.of());
        FinanceOrderMutationVo result = financeOrderRequestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(source.getBookId())
                .orderId(source.getOrderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(command)
                .requestId(command.getRequestId())
                .fileList(List.of())
                .build());
        row.setBillId(result.getBillId());
        billIds.add(result.getBillId());
    }
}
