package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.vo.fortune.bill.BillCategoryAmountVo;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.service.fortune.FortuneBillExtraCategoryValidator;
import com.fortuneboot.service.fortune.exporter.FortuneBillImportExcelExporter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 导出 -> 导入 round-trip 测试
 * <p>
 * 导出器产出的可回导Excel行必须能被真实的导入转换器无错解析，
 * 且分类金额、附加费用等语义保持一致。
 */
class FortuneBillImportExportRoundTripTest {

    private final FortuneCategoryRepo fortuneCategoryRepo = mock(FortuneCategoryRepo.class);

    private final FortuneBillImportExcelExporter exporter = new FortuneBillImportExcelExporter(fortuneCategoryRepo);

    private final FortuneBillImportConverter converter = new FortuneBillImportConverter(
            new FortuneBillExtraCategoryValidator());

    @Test
    @DisplayName("导出的支出账单行可以直接被导入转换器解析回相同语义")
    void exportedExpenseRow_convertsBackWithoutErrors() {
        when(fortuneCategoryRepo.getByIds(List.of(2L))).thenReturn(List.of(category(2L, "手续费分类")));

        List<com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo> rows =
                exporter.toImportExcelRows(List.of(expenseBill()));
        FortuneBillImportRow row = new FortuneBillImportRow(1, rows.get(0));

        converter.convert(row, context());

        assertThat(row.getErrors()).isEmpty();
        FortuneBillAddCommand command = row.getCommand();
        assertThat(command.getTitle()).isEqualTo("午餐");
        assertThat(command.getTradeTime()).isEqualTo(LocalDateTime.of(2026, 6, 24, 12, 0));
        assertThat(command.getBillType()).isEqualTo(BillTypeEnum.EXPENSE.getValue());
        assertThat(command.getAccountId()).isEqualTo(1L);
        assertThat(command.getPayeeId()).isEqualTo(11L);
        assertThat(command.getConfirm()).isTrue();
        assertThat(command.getInclude()).isFalse();
        assertThat(command.getRemark()).isEqualTo("工作餐");

        assertThat(command.getCategoryAmountPair()).hasSize(2);
        assertThat(command.getCategoryAmountPair().get(0).getCategoryId()).isEqualTo(1L);
        assertThat(command.getCategoryAmountPair().get(0).getAmount()).isEqualByComparingTo("12.50");
        assertThat(command.getCategoryAmountPair().get(1).getCategoryId()).isEqualTo(2L);
        assertThat(command.getCategoryAmountPair().get(1).getAmount()).isEqualByComparingTo("7.50");

        List<FortuneBillExtraAddCommand> extras = command.getExtras();
        assertThat(extras).hasSize(2);
        assertThat(extras.get(0).getExtraType()).isEqualTo(1);
        assertThat(extras.get(0).getAmount()).isEqualByComparingTo("2.50");
        assertThat(extras.get(0).getAccountSide()).isEqualTo(1);
        assertThat(extras.get(0).getCategoryId()).isEqualTo(3L);
        assertThat(extras.get(0).getRemark()).isEqualTo("手续费备注");
        assertThat(extras.get(1).getExtraType()).isEqualTo(2);
        assertThat(extras.get(1).getAmount()).isEqualByComparingTo("1.00");
        assertThat(extras.get(1).getAccountSide()).isEqualTo(2);
        assertThat(extras.get(1).getCategoryId()).isEqualTo(1L);
    }

    private FortuneBillBo expenseBill() {
        FortuneBillBo bo = new FortuneBillBo(new FortuneBillEntity());
        bo.setTitle("午餐");
        bo.setTradeTime(LocalDateTime.of(2026, 6, 24, 12, 0));
        bo.setBillType(BillTypeEnum.EXPENSE.getValue());
        bo.setAccountName("现金");
        bo.setPayeeName("便利店");
        bo.setConfirm(Boolean.TRUE);
        bo.setInclude(Boolean.FALSE);
        bo.setRemark("工作餐");
        bo.setCategoryAmountPair(List.of(
                categoryAmount("餐饮", "12.50"),
                categoryAmount("交通", "7.50")));
        bo.setExtras(List.of(
                extra(1, "2.50", 1, 2L, "手续费备注"),
                extra(2, "1.00", 2, null, null)));
        return bo;
    }

    private BillCategoryAmountVo categoryAmount(String name, String amount) {
        BillCategoryAmountVo vo = new BillCategoryAmountVo();
        vo.setCategoryName(name);
        vo.setAmount(new BigDecimal(amount));
        return vo;
    }

    private FortuneBillExtraEntity extra(Integer type, String amount, Integer side, Long categoryId, String remark) {
        FortuneBillExtraEntity entity = new FortuneBillExtraEntity();
        entity.setExtraType(type);
        entity.setAmount(new BigDecimal(amount));
        entity.setAccountSide(side);
        entity.setCategoryId(categoryId);
        entity.setRemark(remark);
        return entity;
    }

    private FortuneBillImportContext context() {
        FortuneBillImportContext context = new FortuneBillImportContext();
        FortuneBookEntity book = new FortuneBookEntity();
        book.setBookId(1L);
        book.setGroupId(1L);
        context.setBookModel(new FortuneBookModel(book, null));

        FortuneAccountEntity account = new FortuneAccountEntity();
        account.setAccountId(1L);
        account.setAccountName("现金");
        context.setAccountMap(Map.of("现金", account));
        context.setAccountIdMap(Map.of(1L, account));

        FortunePayeeEntity payee = new FortunePayeeEntity();
        payee.setPayeeId(11L);
        payee.setPayeeName("便利店");
        context.putPayees(BillTypeEnum.EXPENSE.getValue(), Map.of("便利店", payee));

        context.putCategories(BillTypeEnum.EXPENSE.getValue(), Map.of(
                "餐饮", category(1L, "餐饮"),
                "交通", category(2L, "交通"),
                "手续费分类", category(3L, "手续费分类")));
        return context;
    }

    private FortuneCategoryEntity category(Long id, String name) {
        FortuneCategoryEntity entity = new FortuneCategoryEntity();
        entity.setCategoryId(id);
        entity.setCategoryName(name);
        return entity;
    }
}
