package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneBillImportConverterTest {

    private final FortuneBillImportConverter converter = new FortuneBillImportConverter();

    @Test
    @DisplayName("布尔值支持中文、英文和数字表达")
    void convert_booleanValues_supportMultipleFormats() {
        FortuneBillImportExcelVo source = baseExpense();
        source.setConfirm("TRUE");
        source.setInclude("0");
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);

        converter.convert(row, context());

        assertThat(row.getErrors()).isEmpty();
        assertThat(row.getCommand().getConfirm()).isTrue();
        assertThat(row.getCommand().getInclude()).isFalse();
    }

    @Test
    @DisplayName("预留流水类型不允许导入并聚合错误")
    void convert_reservedBillType_addsError() {
        FortuneBillImportExcelVo source = baseExpense();
        source.setBillType("借出");
        source.setTitle("");
        source.setConfirm("maybe");
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);

        converter.convert(row, context());

        assertThat(row.getErrors()).contains(
                "第2行：标题不能为空",
                "第2行：是否确认【maybe】无法识别",
                "第2行：流水类型【借出】暂不支持导入"
        );
    }

    @Test
    @DisplayName("转账必须填写账户、转入账户和金额")
    void convert_transferMissingAccounts_addsErrors() {
        FortuneBillImportExcelVo source = baseExpense();
        source.setBillType("转账");
        source.setAccount(null);
        source.setToAccount(null);
        source.setAmount(null);
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);

        converter.convert(row, context());

        assertThat(row.getErrors()).contains(
                "第2行：转账必须填写账户",
                "第2行：转账必须填写转入账户",
                "第2行：转账金额不能为空且必须大于0"
        );
    }

    @Test
    @DisplayName("非转账账户为空时允许导入")
    void convert_expenseWithoutAccount_hasNoAccountError() {
        FortuneBillImportExcelVo source = baseExpense();
        source.setAccount(null);
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);

        converter.convert(row, context());

        assertThat(row.getErrors()).isEmpty();
        assertThat(row.getCommand().getAccountId()).isNull();
        assertThat(row.getCommand().getCategoryAmountPair()).hasSize(1);
    }

    @Test
    @DisplayName("优惠金额不能大于或等于账单金额与手续费合计")
    void convert_discountGreaterThanAmount_addsError() {
        FortuneBillImportExcelVo source = baseExpense();
        source.setExtras("优惠:20:转出账户");
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);

        converter.convert(row, context());

        assertThat(row.getErrors()).contains("第2行：优惠金额不能大于或等于账单金额与手续费合计");
    }

    @Test
    @DisplayName("垫付不支持导入附加费用")
    void convert_advanceWithExtras_addsError() {
        FortuneBillImportExcelVo source = baseExpense();
        source.setBillType("垫付");
        source.setOrderId(1L);
        source.setExtras("手续费:1:转出账户");
        FortuneBillImportRow row = new FortuneBillImportRow(2, source);

        converter.convert(row, context());

        assertThat(row.getErrors()).contains("第2行：垫付暂不支持导入附加费用");
    }

    private FortuneBillImportExcelVo baseExpense() {
        FortuneBillImportExcelVo source = new FortuneBillImportExcelVo();
        source.setTitle("午餐");
        source.setTradeTime(LocalDateTime.of(2026, 6, 24, 12, 0));
        source.setBillType("支出");
        source.setAccount("现金");
        source.setCategoryAmounts("餐饮:12.50");
        source.setAmount(new BigDecimal("12.50"));
        source.setConfirm("是");
        source.setInclude("true");
        return source;
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

        FortuneCategoryEntity category = new FortuneCategoryEntity();
        category.setCategoryId(1L);
        category.setCategoryName("餐饮");
        context.putCategories(BillTypeEnum.EXPENSE.getValue(), Map.of("餐饮", category));
        return context;
    }
}
