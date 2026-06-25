package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.factory.fortune.factory.FortuneBillFactory;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.strategy.bill.BillProcessStrategy;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import com.fortuneboot.strategy.bill.BillStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
    private final FortuneBillImportRelationBatch relationBatch;

    void persist(FortuneBillImportRow row, FortuneBillImportContext importContext,
                 FortuneBillImportRelationBatch.Relations relations, List<Long> billIds) {
        try {
            FortuneBillAddCommand command = row.getCommand();
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
}
