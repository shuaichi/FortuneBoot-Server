package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.factory.fortune.model.FortuneBillModel;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FortuneBillImportStrategyContextBuilderTest {

    @Test
    @DisplayName("根据预加载账户构建策略上下文")
    void build_withAccounts_setsContextAccounts() {
        FortuneAccountRepo accountRepo = mock(FortuneAccountRepo.class);
        FortuneBillImportStrategyContextBuilder builder = new FortuneBillImportStrategyContextBuilder(accountRepo);
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setAccountId(1L);
        command.setToAccountId(2L);
        FortuneAccountEntity from = account(1L, "现金");
        FortuneAccountEntity to = account(2L, "银行卡");
        FortuneBillImportContext importContext = context(from, to);

        BillStrategyContext context = builder.build(command, new FortuneBillModel(mock(FortuneBillRepo.class)), importContext);

        assertThat(context.getBookModel().getBookId()).isEqualTo(1L);
        assertThat(context.getFromAccount().getAccountName()).isEqualTo("现金");
        assertThat(context.getToAccount().getAccountName()).isEqualTo("银行卡");
    }

    private FortuneBillImportContext context(FortuneAccountEntity from, FortuneAccountEntity to) {
        FortuneBookEntity book = new FortuneBookEntity();
        book.setBookId(1L);
        FortuneBillImportContext context = new FortuneBillImportContext();
        context.setBookModel(new FortuneBookModel(book, null));
        context.setAccountIdMap(Map.of(1L, from, 2L, to));
        return context;
    }

    private FortuneAccountEntity account(Long id, String name) {
        FortuneAccountEntity account = new FortuneAccountEntity();
        account.setAccountId(id);
        account.setAccountName(name);
        return account;
    }
}
