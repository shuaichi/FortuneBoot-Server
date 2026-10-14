package com.fortuneboot.service.fortune.exporter;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.vo.fortune.bill.BillCategoryAmountVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 可回导账单导出器测试
 */
class FortuneBillImportExcelExporterTest {

    private final FortuneCategoryRepo fortuneCategoryRepo = mock(FortuneCategoryRepo.class);

    private final FortuneBillImportExcelExporter exporter = new FortuneBillImportExcelExporter(fortuneCategoryRepo);

    @Test
    @DisplayName("支出账单按导入模板序列化，金额取分类合计，附加费用解析分类名并回填缺失分类")
    void toImportExcelRows_expenseBill_serializesImportFormat() {
        FortuneBillBo bo = expenseBill();
        when(fortuneCategoryRepo.getByIds(List.of(2L))).thenReturn(List.of(category(2L, "手续费分类")));

        List<FortuneBillImportExcelVo> rows = exporter.toImportExcelRows(List.of(bo));

        assertThat(rows).hasSize(1);
        FortuneBillImportExcelVo row = rows.get(0);
        assertThat(row.getTitle()).isEqualTo("午餐");
        assertThat(row.getTradeTime()).isEqualTo(LocalDateTime.of(2026, 6, 24, 12, 0));
        assertThat(row.getBillType()).isEqualTo("支出");
        assertThat(row.getAccount()).isEqualTo("现金");
        assertThat(row.getPayee()).isEqualTo("便利店");
        assertThat(row.getCategoryAmounts()).isEqualTo("餐饮:12.50；交通:7.50");
        assertThat(row.getAmount()).isEqualByComparingTo("20.00");
        assertThat(row.getTags()).isEqualTo("日常；聚餐");
        assertThat(row.getMembers()).isEqualTo("张三");
        assertThat(row.getConfirm()).isEqualTo("是");
        assertThat(row.getInclude()).isEqualTo("否");
        assertThat(row.getRemark()).isEqualTo("工作餐");
        assertThat(row.getExtras()).isEqualTo("手续费:2.50:转出账户:手续费分类:手续费备注；优惠:1.00:转入账户:餐饮:");
    }

    @Test
    @DisplayName("转账账单直接导出金额，附加费用不回填分类")
    void toImportExcelRows_transferBill_usesDirectAmountWithoutCategoryFallback() {
        FortuneBillBo bo = new FortuneBillBo(new FortuneBillEntity());
        bo.setTitle("转账");
        bo.setBillType(BillTypeEnum.TRANSFER.getValue());
        bo.setAccountName("现金");
        bo.setToAccountName("储蓄卡");
        bo.setAmount(new BigDecimal("100"));
        bo.setConfirm(Boolean.TRUE);
        bo.setInclude(Boolean.TRUE);
        bo.setExtras(List.of(extra(1, "2.50", null, null, null)));

        List<FortuneBillImportExcelVo> rows = exporter.toImportExcelRows(List.of(bo));

        assertThat(rows).hasSize(1);
        FortuneBillImportExcelVo row = rows.get(0);
        assertThat(row.getToAccount()).isEqualTo("储蓄卡");
        assertThat(row.getCategoryAmounts()).isNull();
        assertThat(row.getAmount()).isEqualByComparingTo("100");
        assertThat(row.getExtras()).isEqualTo("手续费:2.50:转出账户::");
        verifyNoInteractions(fortuneCategoryRepo);
    }

    @Test
    @DisplayName("空账单列表直接返回空结果且不查询分类")
    void toImportExcelRows_emptyBills_returnsEmptyWithoutRepoLookup() {
        List<FortuneBillImportExcelVo> rows = exporter.toImportExcelRows(List.of());

        assertThat(rows).isEmpty();
        verifyNoInteractions(fortuneCategoryRepo);
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
        bo.setTagList(List.of(tag("日常"), tag("聚餐")));
        bo.setMemberList(List.of(member("张三")));
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

    private FortuneTagEntity tag(String name) {
        FortuneTagEntity entity = new FortuneTagEntity();
        entity.setTagName(name);
        return entity;
    }

    private FortuneMemberEntity member(String name) {
        FortuneMemberEntity entity = new FortuneMemberEntity();
        entity.setMemberName(name);
        return entity;
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

    private FortuneCategoryEntity category(Long id, String name) {
        FortuneCategoryEntity entity = new FortuneCategoryEntity();
        entity.setCategoryId(id);
        entity.setCategoryName(name);
        return entity;
    }
}
