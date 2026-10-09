package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.enums.fortune.FinanceOrderTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import com.fortuneboot.service.fortune.FortuneBillExtraCategoryValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FinanceOrderAccessGuard {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

    private final FortuneBookRepo fortuneBookRepo;
    private final FortuneFinanceOrderRepo financeOrderRepo;
    private final FortuneBillRepo fortuneBillRepo;
    private final FortuneAccountRepo fortuneAccountRepo;
    private final FortuneCategoryRepo fortuneCategoryRepo;
    private final FortuneTagRepo fortuneTagRepo;
    private final FortuneMemberRepo fortuneMemberRepo;
    private final FortunePayeeRepo fortunePayeeRepo;
    private final FortuneBillExtraCategoryValidator extraCategoryValidator;

    public FortuneBookEntity loadBook(Long bookId) {
        FortuneBookEntity book = fortuneBookRepo.getById(bookId);
        if (book == null || Boolean.TRUE.equals(book.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, bookId, "账本");
        }
        return book;
    }

    public FortuneFinanceOrderEntity loadOrder(Long bookId, Long orderId) {
        FortuneFinanceOrderEntity order = financeOrderRepo.getById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, orderId, "单据");
        }
        if (!Objects.equals(order.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
        return order;
    }

    public FortuneFinanceOrderEntity loadMutableOrder(FortuneBookEntity book, Long orderId,
                                                       boolean allowDraft) {
        FortuneFinanceOrderEntity order = loadOrder(book.getBookId(), orderId);
        if (!Objects.equals(order.getStatus(), FinanceOrderStatusEnum.USING.getValue())
                && !(allowDraft && Objects.equals(order.getStatus(), FinanceOrderStatusEnum.INIT.getValue()))) {
            throw new ApiException(ErrorCode.Business.ORDER_ENTRY_IMMUTABLE);
        }
        if (!"READY".equals(order.getReconciliationStatus())) {
            throw new ApiException(ErrorCode.Business.ORDER_RECONCILIATION_REQUIRED);
        }
        if (order.getCurrencyCode() == null) {
            order.setCurrencyCode(book.getDefaultCurrency());
        }
        if (!orderTypeValid(order.getType())) {
            throw new ApiException(ErrorCode.Business.ORDER_TYPE_MISMATCH);
        }
        return order;
    }

    public FinanceOrderEntryCommand normalizeCommand(FortuneFinanceOrderEntity order,
                                                     FinanceOrderEntryCommand command) {
        if (command == null || command.getBillType() == null || !orderTypeValid(order.getType())) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "流水内容不能为空");
        }
        if (!Objects.equals(command.getBookId(), order.getBookId())
                || !Objects.equals(command.getOrderId(), order.getOrderId())) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
        if (StringUtils.isBlank(command.getTitle()) || command.getTitle().length() > 50
                || command.getTradeTime() == null || command.getConfirm() == null) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "标题、交易时间和确认状态必填");
        }
        FinanceOrderComponentEnum component = FinanceOrderComponentEnum.getByValue(command.getOrderComponent());
        if (component == null) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "单据组件非法");
        }
        command.setBookId(order.getBookId());
        command.setOrderId(order.getOrderId());
        command.setAmount(normalizeAmount(resolveAmount(command, component)));
        command.setConvertedAmount(command.getAmount());
        command.setInclude(Objects.requireNonNullElse(command.getInclude(), Boolean.TRUE));
        if (component == FinanceOrderComponentEnum.WRITE_OFF && command.getAccountId() != null) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "差额结清不能选择账户");
        }
        if (component != FinanceOrderComponentEnum.WRITE_OFF && command.getAccountId() == null) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "现金流水必须选择账户");
        }
        extraCategoryValidator.validate(command);
        validateRelations(command);
        return command;
    }

    public FortuneBillEntity loadBill(FortuneFinanceOrderEntity order, Long billId) {
        FortuneBillEntity bill = fortuneBillRepo.getById(billId);
        if (bill == null || Boolean.TRUE.equals(bill.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, billId, "流水");
        }
        if (!Objects.equals(bill.getBookId(), order.getBookId())
                || !Objects.equals(bill.getOrderId(), order.getOrderId())) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
        if (!Integer.valueOf(1).equals(bill.getOrderLedgerVersion())) {
            throw new ApiException(ErrorCode.Business.ORDER_ENTRY_IMMUTABLE);
        }
        return bill;
    }

    private boolean orderTypeValid(Integer type) {
        return Arrays.stream(FinanceOrderTypeEnum.values())
                .anyMatch(orderType -> Objects.equals(orderType.getValue(), type));
    }

    public FortuneAccountEntity validateTargetAccount(FortuneBookEntity book,
                                                       FortuneFinanceOrderEntity order,
                                                       Long accountId,
                                                       BigDecimal directionAmount,
                                                       boolean requireUsable) {
        FortuneAccountEntity account = fortuneAccountRepo.getById(accountId);
        if (account == null || Boolean.TRUE.equals(account.getDeleted())) {
            throw new ApiException(ErrorCode.Business.COMMON_OBJECT_NOT_FOUND, accountId, "账户");
        }
        if (!Objects.equals(account.getGroupId(), book.getGroupId())) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "账户不属于当前分组");
        }
        if (!Objects.equals(account.getCurrencyCode(), order.getCurrencyCode())) {
            throw new ApiException(ErrorCode.Business.ORDER_CURRENCY_MISMATCH);
        }
        if (requireUsable) {
            if (!Boolean.TRUE.equals(account.getEnable())) {
                throw new ApiException(ErrorCode.Business.BILL_ACCOUNT_DISABLE, account.getAccountName());
            }
            if (directionAmount.compareTo(BigDecimal.ZERO) > 0 && !Boolean.TRUE.equals(account.getCanIncome())) {
                throw new ApiException(ErrorCode.Business.ACCOUNT_CANNOT_INCOME, account.getAccountName());
            }
            if (directionAmount.compareTo(BigDecimal.ZERO) < 0 && !Boolean.TRUE.equals(account.getCanExpense())) {
                throw new ApiException(ErrorCode.Business.ACCOUNT_CANNOT_EXPENSE, account.getAccountName());
            }
        }
        return account;
    }

    private BigDecimal resolveAmount(FinanceOrderEntryCommand command, FinanceOrderComponentEnum component) {
        if (component == FinanceOrderComponentEnum.PRINCIPAL && List.of(9, 10, 11, 12).contains(command.getBillType())) {
            return command.getAmount();
        }
        List<CategoryAmountDTO> categories = command.getCategoryAmountPair();
        if (CollectionUtils.isEmpty(categories)) {
            throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "分类金额不能为空");
        }
        return categories.stream()
                .map(CategoryAmountDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2 || amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ApiException(ErrorCode.Business.ORDER_AMOUNT_INVALID,
                    amount == null ? "null" : amount.toPlainString());
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    private void validateRelations(FinanceOrderEntryCommand command) {
        if (Objects.nonNull(command.getPayeeId())) {
            FortunePayeeEntity payee = fortunePayeeRepo.getById(command.getPayeeId());
            if (payee == null || !Objects.equals(payee.getBookId(), command.getBookId())
                    || !Boolean.TRUE.equals(payee.getEnable())) {
                throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "交易对象不存在或不可用");
            }
        }
        List<CategoryAmountDTO> categories = command.getCategoryAmountPair();
        if (CollectionUtils.isNotEmpty(categories)) {
            List<Long> categoryIds = categories.stream().map(CategoryAmountDTO::getCategoryId).toList();
            var categoryList = fortuneCategoryRepo.listByIds(categoryIds);
            if (categoryList.size() != categoryIds.size() || categoryList.stream().anyMatch(category ->
                    !Objects.equals(category.getBookId(), command.getBookId())
                            || !Boolean.TRUE.equals(category.getEnable()))) {
                throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "分类不存在或不可用");
            }
        }
        if (CollectionUtils.isNotEmpty(command.getTagIdList())) {
            var tagList = fortuneTagRepo.listByIds(command.getTagIdList());
            if (tagList.size() != command.getTagIdList().size() || tagList.stream().anyMatch(tag ->
                    !Objects.equals(tag.getBookId(), command.getBookId())
                            || !Boolean.TRUE.equals(tag.getEnable()))) {
                throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "标签不存在或不可用");
            }
        }
        if (CollectionUtils.isNotEmpty(command.getMemberIdList())) {
            var memberList = fortuneMemberRepo.listByIds(command.getMemberIdList());
            if (memberList.size() != command.getMemberIdList().size() || memberList.stream().anyMatch(member ->
                    !Objects.equals(member.getBookId(), command.getBookId())
                            || !Boolean.TRUE.equals(member.getEnable()))) {
                throw new ApiException(ErrorCode.Business.ORDER_REFERENCE_INVALID, "成员不存在或不可用");
            }
        }
    }
}
