package com.fortuneboot.fortune;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryRelationEntity;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.factory.fortune.factory.FortuneAccountFactory;
import com.fortuneboot.factory.fortune.factory.FortuneBillFactory;
import com.fortuneboot.factory.fortune.factory.FortuneBookFactory;
import com.fortuneboot.factory.fortune.factory.FortuneCategoryFactory;
import com.fortuneboot.factory.fortune.factory.FortuneMemberFactory;
import com.fortuneboot.factory.fortune.factory.FortunePayeeFactory;
import com.fortuneboot.factory.fortune.factory.FortuneTagFactory;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillExtraRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneFileRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import com.fortuneboot.service.fortune.FortuneBillExtraCategoryValidator;
import com.fortuneboot.service.fortune.FortuneBillExtraService;
import com.fortuneboot.service.fortune.FortuneBillService;
import com.fortuneboot.service.fortune.FortuneCategoryRelationService;
import com.fortuneboot.service.fortune.FortuneFileService;
import com.fortuneboot.service.fortune.FortuneMemberRelationService;
import com.fortuneboot.service.fortune.exporter.FortuneBillImportExcelExporter;
import com.fortuneboot.service.fortune.order.FortuneBillOrderPipelineAdapter;
import com.fortuneboot.service.fortune.FortuneTagRelationService;
import com.fortuneboot.strategy.bill.BillStrategyFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 账单列表图标透传测试
 * <p>
 * 账单列表会聚合账户、转入账户、交易对象和分类的展示信息，
 * 图标必须与名称一起被批量填充，供前端列表与导出场景直接消费。
 */
class FortuneBillServiceIconTest {

    private final FortuneBillRepo fortuneBillRepo = mock(FortuneBillRepo.class);
    private final FortuneBillFactory fortuneBillFactory = mock(FortuneBillFactory.class);
    private final FortuneAccountFactory fortuneAccountFactory = mock(FortuneAccountFactory.class);
    private final FortuneTagRelationService fortuneTagRelationService = mock(FortuneTagRelationService.class);
    private final FortunePayeeFactory fortunePayeeFactory = mock(FortunePayeeFactory.class);
    private final FortuneCategoryRepo fortuneCategoryRepo = mock(FortuneCategoryRepo.class);
    private final FortuneCategoryRelationService fortuneCategoryRelationService =
        mock(FortuneCategoryRelationService.class);
    private final FortunePayeeRepo fortunePayeeRepo = mock(FortunePayeeRepo.class);
    private final FortuneBookRepo fortuneBookRepo = mock(FortuneBookRepo.class);
    private final FortuneAccountRepo fortuneAccountRepo = mock(FortuneAccountRepo.class);
    private final FortuneCategoryRelationRepo fortuneCategoryRelationRepo = mock(FortuneCategoryRelationRepo.class);
    private final FortuneTagRelationRepo fortuneTagRelationRepo = mock(FortuneTagRelationRepo.class);
    private final FortuneTagRepo fortuneTagRepo = mock(FortuneTagRepo.class);
    private final FortuneFileRepo fortuneFileRepo = mock(FortuneFileRepo.class);
    private final FortuneTagFactory fortuneTagFactory = mock(FortuneTagFactory.class);
    private final FortuneCategoryFactory fortuneCategoryFactory = mock(FortuneCategoryFactory.class);
    private final FortuneBookFactory fortuneBookFactory = mock(FortuneBookFactory.class);
    private final BillStrategyFactory strategyFactory = mock(BillStrategyFactory.class);
    private final FortuneMemberRelationRepo fortuneMemberRelationRepo = mock(FortuneMemberRelationRepo.class);
    private final FortuneMemberRepo fortuneMemberRepo = mock(FortuneMemberRepo.class);
    private final FortuneMemberRelationService fortuneMemberRelationService = mock(FortuneMemberRelationService.class);
    private final FortuneMemberFactory fortuneMemberFactory = mock(FortuneMemberFactory.class);
    private final FortuneBillExtraService fortuneBillExtraService = mock(FortuneBillExtraService.class);
    private final FortuneBillExtraRepo fortuneBillExtraRepo = mock(FortuneBillExtraRepo.class);
    private final FortuneBillExtraCategoryValidator extraCategoryValidator =
        mock(FortuneBillExtraCategoryValidator.class);

    private final FortuneBillService fortuneBillService = new FortuneBillService(
        fortuneBillRepo,
        mock(FortuneFinanceOrderRepo.class),
        fortuneBillFactory,
        fortuneAccountFactory,
        fortuneTagRelationService,
        fortunePayeeFactory,
        fortuneCategoryRepo,
        fortuneCategoryRelationService,
        fortunePayeeRepo,
        fortuneBookRepo,
        fortuneAccountRepo,
        fortuneCategoryRelationRepo,
        fortuneTagRelationRepo,
        fortuneTagRepo,
        mock(FortuneFileService.class),
        fortuneFileRepo,
        fortuneTagFactory,
        fortuneCategoryFactory,
        fortuneBookFactory,
        strategyFactory,
        fortuneMemberRelationRepo,
        fortuneMemberRepo,
        fortuneMemberRelationService,
        fortuneMemberFactory,
        fortuneBillExtraService,
        fortuneBillExtraRepo,
        extraCategoryValidator,
        mock(FortuneBillOrderPipelineAdapter.class),
        mock(FortuneBillImportExcelExporter.class));

    @Test
    @DisplayName("账单列表应批量填充账户、转入账户、交易对象和分类图标")
    void getPage_fillsIconsFromRelatedEntities() {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBillId(1L);
        bill.setBookId(1L);
        bill.setAccountId(10L);
        bill.setToAccountId(20L);
        bill.setPayeeId(30L);
        Page<FortuneBillEntity> page = new Page<>(1, 10);
        page.setRecords(List.of(bill));
        page.setTotal(1);
        when(fortuneBillRepo.getPage(any(), any())).thenReturn(page);

        FortuneAccountEntity account = new FortuneAccountEntity();
        account.setAccountId(10L);
        account.setAccountName("现金");
        account.setIcon("ep:wallet");
        account.setCurrencyCode("CNY");
        FortuneAccountEntity toAccount = new FortuneAccountEntity();
        toAccount.setAccountId(20L);
        toAccount.setAccountName("储蓄卡");
        toAccount.setIcon("ep:credit-card");
        toAccount.setCurrencyCode("CNY");
        when(fortuneAccountRepo.getByIds(anyList())).thenReturn(List.of(account, toAccount));

        FortuneBookEntity book = new FortuneBookEntity();
        book.setBookId(1L);
        book.setBookName("默认账本");
        when(fortuneBookRepo.listByIds(anyList())).thenReturn(List.of(book));

        FortunePayeeEntity payee = new FortunePayeeEntity();
        payee.setPayeeId(30L);
        payee.setPayeeName("超市");
        payee.setIcon("ep:shop");
        when(fortunePayeeRepo.getByIdList(anyList())).thenReturn(List.of(payee));

        FortuneCategoryRelationEntity relation = new FortuneCategoryRelationEntity();
        relation.setBillId(1L);
        relation.setCategoryId(100L);
        relation.setAmount(new BigDecimal("50"));
        when(fortuneCategoryRelationRepo.getByBillIdList(anyList()))
            .thenReturn(Map.of(1L, List.of(relation)));
        FortuneCategoryEntity category = new FortuneCategoryEntity();
        category.setCategoryId(100L);
        category.setCategoryName("餐饮");
        category.setIcon("ep:menu");
        when(fortuneCategoryRepo.getByIds(anyList())).thenReturn(List.of(category));

        when(fortuneTagRelationRepo.getByBillIdList(anyList())).thenReturn(Map.of());
        when(fortuneMemberRelationRepo.getByBillIdList(anyList())).thenReturn(Map.of());
        when(fortuneFileRepo.findBillIdsWithFiles(anyList())).thenReturn(Set.of());
        when(fortuneBillExtraRepo.getByBillIdList(anyList())).thenReturn(Map.of());

        FortuneBillQuery query = new FortuneBillQuery();
        query.setBookId(1L);
        PageDTO<FortuneBillBo> result = fortuneBillService.getPage(query);

        assertThat(result.getRows()).hasSize(1);
        FortuneBillBo billBo = result.getRows().get(0);
        assertThat(billBo.getAccountName()).isEqualTo("现金");
        assertThat(billBo.getAccountIcon()).isEqualTo("ep:wallet");
        assertThat(billBo.getToAccountName()).isEqualTo("储蓄卡");
        assertThat(billBo.getToAccountIcon()).isEqualTo("ep:credit-card");
        assertThat(billBo.getPayeeName()).isEqualTo("超市");
        assertThat(billBo.getPayeeIcon()).isEqualTo("ep:shop");
        assertThat(billBo.getCategoryAmountPair()).hasSize(1);
        assertThat(billBo.getCategoryAmountPair().get(0).getCategoryName()).isEqualTo("餐饮");
        assertThat(billBo.getCategoryAmountPair().get(0).getIcon()).isEqualTo("ep:menu");
    }

    @Test
    @DisplayName("账户或转入账户不存在时图标应回退为空串而不是报错")
    void getPage_fallsBackToEmptyIconWhenAccountMissing() {
        FortuneBillEntity bill = new FortuneBillEntity();
        bill.setBillId(1L);
        bill.setBookId(1L);
        bill.setAccountId(10L);
        Page<FortuneBillEntity> page = new Page<>(1, 10);
        page.setRecords(List.of(bill));
        page.setTotal(1);
        when(fortuneBillRepo.getPage(any(), any())).thenReturn(page);
        when(fortuneAccountRepo.getByIds(anyList())).thenReturn(List.of());

        FortuneBookEntity book = new FortuneBookEntity();
        book.setBookId(1L);
        book.setBookName("默认账本");
        when(fortuneBookRepo.listByIds(anyList())).thenReturn(List.of(book));
        when(fortuneCategoryRelationRepo.getByBillIdList(anyList())).thenReturn(Map.of());
        when(fortuneCategoryRepo.getByIds(anyList())).thenReturn(List.of());
        when(fortuneTagRelationRepo.getByBillIdList(anyList())).thenReturn(Map.of());
        when(fortuneMemberRelationRepo.getByBillIdList(anyList())).thenReturn(Map.of());
        when(fortuneFileRepo.findBillIdsWithFiles(anyList())).thenReturn(Set.of());
        when(fortuneBillExtraRepo.getByBillIdList(anyList())).thenReturn(Map.of());

        FortuneBillQuery query = new FortuneBillQuery();
        query.setBookId(1L);
        PageDTO<FortuneBillBo> result = fortuneBillService.getPage(query);

        assertThat(result.getRows()).hasSize(1);
        FortuneBillBo billBo = result.getRows().get(0);
        assertThat(billBo.getAccountIcon()).isEmpty();
        assertThat(billBo.getToAccountIcon()).isEmpty();
    }
}
