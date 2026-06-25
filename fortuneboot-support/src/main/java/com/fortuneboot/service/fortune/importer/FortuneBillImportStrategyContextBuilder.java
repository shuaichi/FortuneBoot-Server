package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author zhangchi118
 */
@Component
@RequiredArgsConstructor
class FortuneBillImportStrategyContextBuilder {

    private final FortuneAccountRepo fortuneAccountRepo;

    BillStrategyContext build(FortuneBillAddCommand command, FortuneBillModel billModel, FortuneBillImportContext importContext) {
        BillStrategyContext context = new BillStrategyContext();
        context.setCommand(command);
        context.setBillModel(billModel);
        context.setBookModel(importContext.getBookModel());
        if (Objects.nonNull(command.getAccountId())) {
            FortuneAccountEntity account = importContext.getAccountIdMap().get(command.getAccountId());
            context.setFromAccount(new FortuneAccountModel(account, fortuneAccountRepo));
        }
        if (Objects.nonNull(command.getToAccountId())) {
            FortuneAccountEntity toAccount = importContext.getAccountIdMap().get(command.getToAccountId());
            context.setToAccount(new FortuneAccountModel(toAccount, fortuneAccountRepo));
        }
        return context;
    }
}
