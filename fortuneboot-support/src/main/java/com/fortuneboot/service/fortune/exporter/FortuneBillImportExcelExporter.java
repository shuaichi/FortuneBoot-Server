package com.fortuneboot.service.fortune.exporter;

import com.fortuneboot.common.enums.fortune.BillExtraAccountSideEnum;
import com.fortuneboot.common.enums.fortune.BillExtraTypeEnum;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.entity.fortune.FortuneBillExtraEntity;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.vo.fortune.bill.BillCategoryAmountVo;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 可回导游账单导出器
 * <p>
 * 将账单组装为与导入模板（FortuneBillImportExcelVo）同构的Excel行，
 * 保证「导出的Excel」可以直接走导入流程回导。
 * 导入导出共享的文案与编码统一由 BillTypeEnum / BillExtraTypeEnum /
 * BillExtraAccountSideEnum 承载，避免两端契约漂移。
 *
 * @author fortuneboot
 */
@Component
public class FortuneBillImportExcelExporter {

    /**
     * 导入模板多值单元格的稳定分隔符，与导入解析约定一致
     */
    private static final String ITEM_SEPARATOR = "；";
    private static final String PART_SEPARATOR = ":";
    private static final String BOOLEAN_TRUE_TEXT = "是";
    private static final String BOOLEAN_FALSE_TEXT = "否";

    private final FortuneCategoryRepo fortuneCategoryRepo;

    public FortuneBillImportExcelExporter(FortuneCategoryRepo fortuneCategoryRepo) {
        this.fortuneCategoryRepo = fortuneCategoryRepo;
    }

    public List<FortuneBillImportExcelVo> toImportExcelRows(List<FortuneBillBo> bills) {
        if (CollectionUtils.isEmpty(bills)) {
            return Collections.emptyList();
        }
        Map<Long, String> extraCategoryNames = this.loadExtraCategoryNames(bills);
        return bills.stream().map(bo -> this.toImportExcelVo(bo, extraCategoryNames)).toList();
    }

    /**
     * 批量解析附加费用（手续费/优惠）关联的分类名称，导出时序列化为导入格式需要名称而非id
     */
    private Map<Long, String> loadExtraCategoryNames(List<FortuneBillBo> bills) {
        List<Long> categoryIds = bills.stream()
                .map(FortuneBillBo::getExtras)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .map(FortuneBillExtraEntity::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        return fortuneCategoryRepo.getByIds(categoryIds).stream()
                .collect(Collectors.toMap(FortuneCategoryEntity::getCategoryId, FortuneCategoryEntity::getCategoryName));
    }

    private FortuneBillImportExcelVo toImportExcelVo(FortuneBillBo bo, Map<Long, String> extraCategoryNameMap) {
        FortuneBillImportExcelVo vo = new FortuneBillImportExcelVo();
        vo.setTitle(bo.getTitle());
        vo.setTradeTime(bo.getTradeTime());
        vo.setBillType(BillTypeEnum.getDescByValue(bo.getBillType()));
        vo.setAccount(bo.getAccountName());
        vo.setToAccount(bo.getToAccountName());
        vo.setOrderId(bo.getOrderId());
        vo.setPayee(bo.getPayeeName());
        vo.setTags(this.joinNames(bo.getTagList(), FortuneTagEntity::getTagName));
        vo.setMembers(this.joinNames(bo.getMemberList(), FortuneMemberEntity::getMemberName));
        vo.setConfirm(Boolean.TRUE.equals(bo.getConfirm()) ? BOOLEAN_TRUE_TEXT : BOOLEAN_FALSE_TEXT);
        vo.setInclude(Boolean.TRUE.equals(bo.getInclude()) ? BOOLEAN_TRUE_TEXT : BOOLEAN_FALSE_TEXT);
        vo.setRemark(bo.getRemark());

        boolean transfer = Objects.equals(BillTypeEnum.TRANSFER.getValue(), bo.getBillType());
        if (transfer) {
            vo.setAmount(bo.getAmount());
        } else if (CollectionUtils.isNotEmpty(bo.getCategoryAmountPair())) {
            vo.setCategoryAmounts(bo.getCategoryAmountPair().stream()
                    .filter(item -> StringUtils.isNotBlank(item.getCategoryName()) && Objects.nonNull(item.getAmount()))
                    .map(item -> item.getCategoryName() + PART_SEPARATOR + item.getAmount())
                    .collect(Collectors.joining(ITEM_SEPARATOR)));
            // 金额必须等于分类金额合计，直接取合计保证导入校验通过
            vo.setAmount(bo.getCategoryAmountPair().stream()
                    .map(BillCategoryAmountVo::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        // 非转账账单的附加费用在导入时必须有分类（BILL_EXTRA_CATEGORY_REQUIRED）。
        // 历史数据存在分类为空的情况，导出时按 Flyway V1.7.1 相同的策略回填账单首个分类，
        // 保证导出的文件一定能被导入，不会出现“导出成功但导入报错”的断层。
        vo.setExtras(this.serializeExtras(bo, extraCategoryNameMap, transfer ? null : this.firstCategoryName(bo)));
        return vo;
    }

    /**
     * 取账单的首个有效分类名称，作为非转账账单附加费用缺失分类时的回填值（与 Flyway V1.7.1 策略一致）
     */
    private String firstCategoryName(FortuneBillBo bo) {
        if (CollectionUtils.isEmpty(bo.getCategoryAmountPair())) {
            return null;
        }
        return bo.getCategoryAmountPair().stream()
                .map(BillCategoryAmountVo::getCategoryName)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    /**
     * 附加费用序列化为导入格式：类型:金额:账户方向:分类:备注
     *
     * @param fallbackCategoryName 非转账账单在附加费用分类缺失时的回填分类名，转账账单传 null
     */
    private String serializeExtras(FortuneBillBo bo, Map<Long, String> extraCategoryNameMap, String fallbackCategoryName) {
        if (CollectionUtils.isEmpty(bo.getExtras())) {
            return null;
        }
        return bo.getExtras().stream()
                .filter(Objects::nonNull)
                .map(extra -> {
                    BillExtraTypeEnum type = Objects.equals(BillExtraTypeEnum.DISCOUNT.getValue(), extra.getExtraType())
                            ? BillExtraTypeEnum.DISCOUNT
                            : BillExtraTypeEnum.FEE;
                    BillExtraAccountSideEnum side = Objects.equals(BillExtraAccountSideEnum.TO.getValue(), extra.getAccountSide())
                            ? BillExtraAccountSideEnum.TO
                            : BillExtraAccountSideEnum.FROM;
                    String category = Objects.nonNull(extra.getCategoryId())
                            ? StringUtils.trimToEmpty(extraCategoryNameMap.get(extra.getCategoryId()))
                            : StringUtils.EMPTY;
                    if (StringUtils.isBlank(category) && StringUtils.isNotBlank(fallbackCategoryName)) {
                        category = fallbackCategoryName;
                    }
                    String remark = StringUtils.trimToEmpty(extra.getRemark());
                    return type.getDescription() + PART_SEPARATOR + extra.getAmount() + PART_SEPARATOR
                            + side.getDescription() + PART_SEPARATOR + category + PART_SEPARATOR + remark;
                })
                .collect(Collectors.joining(ITEM_SEPARATOR));
    }

    private <T> String joinNames(List<T> list, Function<T, String> nameGetter) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().map(nameGetter).filter(StringUtils::isNotBlank).collect(Collectors.joining(ITEM_SEPARATOR));
    }
}
