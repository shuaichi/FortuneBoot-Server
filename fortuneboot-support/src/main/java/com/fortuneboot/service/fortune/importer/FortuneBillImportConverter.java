package com.fortuneboot.service.fortune.importer;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillExtraAddCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author zhangchi118
 */
@Component
@RequiredArgsConstructor
public class FortuneBillImportConverter {

    private static final Set<String> TRUE_VALUES = Set.of("是", "真", "true", "1", "yes", "y");
    private static final Set<String> FALSE_VALUES = Set.of("否", "假", "false", "0", "no", "n");
    private static final Set<Integer> SUPPORTED_BILL_TYPES = Set.of(
            BillTypeEnum.EXPENSE.getValue(),
            BillTypeEnum.INCOME.getValue(),
            BillTypeEnum.TRANSFER.getValue(),
            BillTypeEnum.ADVANCE.getValue(),
            BillTypeEnum.REIMBURSE.getValue()
    );
    private static final int EXTRA_TYPE_FEE = 1;
    private static final int EXTRA_TYPE_DISCOUNT = 2;
    private static final int ACCOUNT_SIDE_FROM = 1;
    private static final int ACCOUNT_SIDE_TO = 2;

    void convert(FortuneBillImportRow row, FortuneBillImportContext context) {
        FortuneBillImportExcelVo source = row.getSource();
        FortuneBillAddCommand command = new FortuneBillAddCommand();
        command.setBookId(context.getBookModel().getBookId());
        command.setTitle(StringUtils.trimToNull(source.getTitle()));
        command.setTradeTime(source.getTradeTime());
        command.setOrderId(source.getOrderId());
        command.setRemark(StringUtils.trimToNull(source.getRemark()));
        command.setConfirm(parseBoolean(source.getConfirm(), Boolean.TRUE, row, "是否确认"));
        command.setInclude(parseBoolean(source.getInclude(), Boolean.TRUE, row, "是否统计"));
        command.setFileList(Collections.emptyList());

        validateTitle(row, command.getTitle());
        validateTradeTime(row, command);

        Integer billType = parseBillType(source.getBillType(), row);
        command.setBillType(billType);
        if (Objects.nonNull(billType)) {
            fillAccounts(row, context, command);
            fillPayee(row, context, command);
            fillCategories(row, context, command);
            fillTags(row, context, command);
            fillMembers(row, context, command);
            fillExtras(row, context, command);
            validateExtraNetAmount(row, command, source);
            validateTypeRules(row, command, source);
        }

        row.setCommand(command);
    }

    boolean isBlankRow(FortuneBillImportExcelVo source) {
        if (Objects.isNull(source)) {
            return true;
        }
        return StringUtils.isAllBlank(
                source.getTitle(),
                source.getBillType(),
                source.getAccount(),
                source.getToAccount(),
                source.getPayee(),
                source.getCategoryAmounts(),
                source.getTags(),
                source.getMembers(),
                source.getConfirm(),
                source.getInclude(),
                source.getRemark(),
                source.getExtras(),
                source.getFiles()
        ) && Objects.isNull(source.getTradeTime())
                && Objects.isNull(source.getOrderId())
                && Objects.isNull(source.getAmount());
    }

    private void validateTitle(FortuneBillImportRow row, String title) {
        if (StringUtils.isBlank(title)) {
            row.addError("第" + row.getRowNum() + "行：标题不能为空");
            return;
        }
        if (title.length() > 50) {
            row.addError("第" + row.getRowNum() + "行：标题长度不能超过50个字符");
        }
    }

    private void validateTradeTime(FortuneBillImportRow row, FortuneBillAddCommand command) {
        if (Objects.isNull(command.getTradeTime())) {
            row.addError("第" + row.getRowNum() + "行：交易时间不能为空");
        }
    }

    private Integer parseBillType(String value, FortuneBillImportRow row) {
        String text = StringUtils.trimToNull(value);
        if (StringUtils.isBlank(text)) {
            row.addError("第" + row.getRowNum() + "行：流水类型不能为空");
            return null;
        }
        BillTypeEnum billType = Arrays.stream(BillTypeEnum.values())
                .filter(item -> Objects.equals(item.getDescription(), text))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(billType)) {
            row.addError("第" + row.getRowNum() + "行：流水类型【" + text + "】不存在");
            return null;
        }
        if (!SUPPORTED_BILL_TYPES.contains(billType.getValue())) {
            row.addError("第" + row.getRowNum() + "行：流水类型【" + text + "】暂不支持导入");
            return null;
        }
        return billType.getValue();
    }

    private Boolean parseBoolean(String value, Boolean defaultValue, FortuneBillImportRow row, String fieldName) {
        String text = StringUtils.trimToNull(value);
        if (StringUtils.isBlank(text)) {
            return defaultValue;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (TRUE_VALUES.contains(lower)) {
            return Boolean.TRUE;
        }
        if (FALSE_VALUES.contains(lower)) {
            return Boolean.FALSE;
        }
        row.addError("第" + row.getRowNum() + "行：" + fieldName + "【" + text + "】无法识别");
        return defaultValue;
    }

    private void fillAccounts(FortuneBillImportRow row, FortuneBillImportContext context, FortuneBillAddCommand command) {
        FortuneBillImportExcelVo source = row.getSource();
        FortuneAccountEntity account = findAccount(row, context, source.getAccount(), "账户");
        FortuneAccountEntity toAccount = findAccount(row, context, source.getToAccount(), "转入账户");
        if (Objects.nonNull(account)) {
            command.setAccountId(account.getAccountId());
        }
        if (Objects.nonNull(toAccount)) {
            command.setToAccountId(toAccount.getAccountId());
        }
    }

    private FortuneAccountEntity findAccount(FortuneBillImportRow row, FortuneBillImportContext context, String value, String fieldName) {
        String name = StringUtils.trimToNull(value);
        if (StringUtils.isBlank(name)) {
            return null;
        }
        FortuneAccountEntity account = context.getAccountMap().get(name);
        if (Objects.isNull(account)) {
            row.addError("第" + row.getRowNum() + "行：" + fieldName + "【" + name + "】不存在或未启用");
        }
        return account;
    }

    private void fillPayee(FortuneBillImportRow row, FortuneBillImportContext context, FortuneBillAddCommand command) {
        String name = StringUtils.trimToNull(row.getSource().getPayee());
        if (StringUtils.isBlank(name)) {
            return;
        }
        FortunePayeeEntity payee = context.getPayee(command.getBillType(), name);
        if (Objects.isNull(payee)) {
            row.addError("第" + row.getRowNum() + "行：交易对象【" + name + "】不存在或未启用");
            return;
        }
        command.setPayeeId(payee.getPayeeId());
    }

    private void fillCategories(FortuneBillImportRow row, FortuneBillImportContext context, FortuneBillAddCommand command) {
        if (Objects.equals(command.getBillType(), BillTypeEnum.TRANSFER.getValue())) {
            command.setCategoryAmountPair(Collections.emptyList());
            return;
        }
        String text = StringUtils.trimToNull(row.getSource().getCategoryAmounts());
        if (StringUtils.isBlank(text)) {
            row.addError("第" + row.getRowNum() + "行：分类金额不能为空");
            command.setCategoryAmountPair(Collections.emptyList());
            return;
        }
        List<CategoryAmountDTO> categories = new ArrayList<>();
        for (String item : splitItems(text)) {
            String[] parts = item.split("[:：]", 2);
            if (parts.length != 2 || StringUtils.isBlank(parts[0]) || StringUtils.isBlank(parts[1])) {
                row.addError("第" + row.getRowNum() + "行：分类金额【" + item + "】格式错误，正确格式为 分类:金额");
                continue;
            }
            String categoryName = StringUtils.trim(parts[0]);
            BigDecimal amount = parsePositiveAmount(row, parts[1], "分类【" + categoryName + "】金额");
            FortuneCategoryEntity category = context.getCategory(command.getBillType(), categoryName);
            if (Objects.isNull(category)) {
                row.addError("第" + row.getRowNum() + "行：分类【" + categoryName + "】不存在或未启用");
                continue;
            }
            if (Objects.nonNull(amount)) {
                categories.add(new CategoryAmountDTO(category.getCategoryId(), amount));
            }
        }
        command.setCategoryAmountPair(categories);
        validateAmount(row, command, row.getSource().getAmount());
    }

    private void fillTags(FortuneBillImportRow row, FortuneBillImportContext context, FortuneBillAddCommand command) {
        List<Long> tagIds = new ArrayList<>();
        for (String name : splitNames(row.getSource().getTags())) {
            FortuneTagEntity tag = context.getTag(command.getBillType(), name);
            if (Objects.isNull(tag)) {
                row.addError("第" + row.getRowNum() + "行：标签【" + name + "】不存在或未启用");
                continue;
            }
            tagIds.add(tag.getTagId());
        }
        command.setTagIdList(tagIds.stream().distinct().toList());
    }

    private void fillMembers(FortuneBillImportRow row, FortuneBillImportContext context, FortuneBillAddCommand command) {
        List<Long> memberIds = new ArrayList<>();
        for (String name : splitNames(row.getSource().getMembers())) {
            FortuneMemberEntity member = context.getMemberMap().get(name);
            if (Objects.isNull(member)) {
                row.addError("第" + row.getRowNum() + "行：成员【" + name + "】不存在或未启用");
                continue;
            }
            memberIds.add(member.getMemberId());
        }
        command.setMemberIdList(memberIds.stream().distinct().toList());
    }

    private void fillExtras(FortuneBillImportRow row, FortuneBillImportContext context, FortuneBillAddCommand command) {
        String text = StringUtils.trimToNull(row.getSource().getExtras());
        if (StringUtils.isBlank(text)) {
            command.setExtras(Collections.emptyList());
            return;
        }
        List<FortuneBillExtraAddCommand> extras = new ArrayList<>();
        for (String item : splitItems(text)) {
            String[] parts = item.split("[:：]", -1);
            if (parts.length < 2) {
                row.addError("第" + row.getRowNum() + "行：附加费用【" + item + "】格式错误，正确格式为 类型:金额:账户方向:分类:备注");
                continue;
            }
            FortuneBillExtraAddCommand extra = new FortuneBillExtraAddCommand();
            extra.setExtraType(parseExtraType(row, parts[0]));
            extra.setAmount(parsePositiveAmount(row, parts[1], "附加费用金额"));
            extra.setAccountSide(parts.length > 2 && StringUtils.isNotBlank(parts[2]) ? parseAccountSide(row, parts[2]) : ACCOUNT_SIDE_FROM);
            if (parts.length > 3 && StringUtils.isNotBlank(parts[3])) {
                FortuneCategoryEntity category = context.getCategory(command.getBillType(), StringUtils.trim(parts[3]));
                if (Objects.isNull(category)) {
                    row.addError("第" + row.getRowNum() + "行：附加费用分类【" + StringUtils.trim(parts[3]) + "】不存在或未启用");
                } else {
                    extra.setCategoryId(category.getCategoryId());
                }
            }
            if (parts.length > 4) {
                extra.setRemark(StringUtils.trimToNull(parts[4]));
            }
            if (Objects.nonNull(extra.getExtraType()) && Objects.nonNull(extra.getAmount()) && Objects.nonNull(extra.getAccountSide())) {
                extras.add(extra);
            }
        }
        command.setExtras(extras);
    }

    private Integer parseExtraType(FortuneBillImportRow row, String value) {
        String text = StringUtils.trim(value);
        if (Objects.equals(text, "手续费")) {
            return EXTRA_TYPE_FEE;
        }
        if (Objects.equals(text, "优惠")) {
            return EXTRA_TYPE_DISCOUNT;
        }
        row.addError("第" + row.getRowNum() + "行：附加费用类型【" + text + "】仅支持手续费或优惠");
        return null;
    }

    private Integer parseAccountSide(FortuneBillImportRow row, String value) {
        String text = StringUtils.trim(value);
        if (Objects.equals(text, "转出账户")) {
            return ACCOUNT_SIDE_FROM;
        }
        if (Objects.equals(text, "转入账户")) {
            return ACCOUNT_SIDE_TO;
        }
        row.addError("第" + row.getRowNum() + "行：附加费用账户方向【" + text + "】仅支持转出账户或转入账户");
        return null;
    }

    private BigDecimal parsePositiveAmount(FortuneBillImportRow row, String value, String fieldName) {
        try {
            BigDecimal amount = new BigDecimal(StringUtils.trim(value));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                row.addError("第" + row.getRowNum() + "行：" + fieldName + "必须大于0");
                return null;
            }
            return amount;
        } catch (NumberFormatException e) {
            row.addError("第" + row.getRowNum() + "行：" + fieldName + "【" + value + "】不是有效金额");
            return null;
        }
    }

    private void validateAmount(FortuneBillImportRow row, FortuneBillAddCommand command, BigDecimal amount) {
        if (Objects.isNull(amount) || CollectionUtils.isEmpty(command.getCategoryAmountPair())) {
            return;
        }
        BigDecimal total = command.getCategoryAmountPair().stream()
                .map(CategoryAmountDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount.compareTo(total) != 0) {
            row.addError("第" + row.getRowNum() + "行：金额必须等于分类金额合计");
        }
    }

    private void validateExtraNetAmount(FortuneBillImportRow row, FortuneBillAddCommand command, FortuneBillImportExcelVo source) {
        if (CollectionUtils.isEmpty(command.getExtras())) {
            return;
        }
        if (Objects.equals(command.getBillType(), BillTypeEnum.INCOME.getValue())
                || Objects.equals(command.getBillType(), BillTypeEnum.ADVANCE.getValue())
                || Objects.equals(command.getBillType(), BillTypeEnum.REIMBURSE.getValue())) {
            row.addError("第" + row.getRowNum() + "行：" + BillTypeEnum.getDescByValue(command.getBillType()) + "暂不支持导入附加费用");
            return;
        }
        if (Objects.equals(command.getBillType(), BillTypeEnum.TRANSFER.getValue())) {
            validateTransferFromSideNetAmount(row, source.getAmount(), command.getExtras());
            return;
        }
        BigDecimal amount = command.getCategoryAmountPair().stream()
                .map(CategoryAmountDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal fee = sumExtras(command.getExtras(), EXTRA_TYPE_FEE, null);
        BigDecimal discount = sumExtras(command.getExtras(), EXTRA_TYPE_DISCOUNT, null);
        if (amount.add(fee).subtract(discount).compareTo(BigDecimal.ZERO) <= 0) {
            row.addError("第" + row.getRowNum() + "行：优惠金额不能大于或等于账单金额与手续费合计");
        }
    }

    private void validateTransferFromSideNetAmount(FortuneBillImportRow row, BigDecimal amount, List<FortuneBillExtraAddCommand> extras) {
        if (Objects.isNull(amount)) {
            return;
        }
        BigDecimal fromFee = sumExtras(extras, EXTRA_TYPE_FEE, ACCOUNT_SIDE_FROM);
        BigDecimal fromDiscount = sumExtras(extras, EXTRA_TYPE_DISCOUNT, ACCOUNT_SIDE_FROM);
        if (amount.add(fromFee).subtract(fromDiscount).compareTo(BigDecimal.ZERO) <= 0) {
            row.addError("第" + row.getRowNum() + "行：转出账户侧优惠金额不能大于或等于转账金额与手续费合计");
        }
    }

    private BigDecimal sumExtras(List<FortuneBillExtraAddCommand> extras, Integer extraType, Integer accountSide) {
        return extras.stream()
                .filter(extra -> Objects.equals(extra.getExtraType(), extraType))
                .filter(extra -> Objects.isNull(accountSide) || Objects.equals(extra.getAccountSide(), accountSide))
                .map(FortuneBillExtraAddCommand::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateTypeRules(FortuneBillImportRow row, FortuneBillAddCommand command, FortuneBillImportExcelVo source) {
        if (Objects.equals(command.getBillType(), BillTypeEnum.TRANSFER.getValue())) {
            if (Objects.isNull(command.getAccountId())) {
                row.addError("第" + row.getRowNum() + "行：转账必须填写账户");
            }
            if (Objects.isNull(command.getToAccountId())) {
                row.addError("第" + row.getRowNum() + "行：转账必须填写转入账户");
            }
            if (Objects.nonNull(command.getAccountId()) && Objects.equals(command.getAccountId(), command.getToAccountId())) {
                row.addError("第" + row.getRowNum() + "行：转出账户和转入账户不能相同");
            }
            if (Objects.isNull(source.getAmount()) || source.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                row.addError("第" + row.getRowNum() + "行：转账金额不能为空且必须大于0");
            } else {
                command.setAmount(source.getAmount());
            }
        }
        if ((Objects.equals(command.getBillType(), BillTypeEnum.ADVANCE.getValue())
                || Objects.equals(command.getBillType(), BillTypeEnum.REIMBURSE.getValue()))
                && Objects.isNull(command.getOrderId())) {
            row.addError("第" + row.getRowNum() + "行：垫付和报销必须填写单据ID");
        }
        if (StringUtils.isNotBlank(source.getFiles())) {
            row.addError("第" + row.getRowNum() + "行：账单Excel导入暂不支持附件，请清空后重试");
        }
    }

    private List<String> splitNames(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split("[；;，,、]"))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> splitItems(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        return Arrays.stream(text.split("[；;]"))
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }
}
