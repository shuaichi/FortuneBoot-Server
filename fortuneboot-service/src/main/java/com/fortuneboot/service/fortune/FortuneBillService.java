package com.fortuneboot.service.fortune;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.BalanceOperationEnum;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.CurrencyTemplateBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.factory.fortune.*;
import com.fortuneboot.factory.fortune.model.*;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 账单service
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:42
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBillService {

    private final FortuneBillRepository fortuneBillRepository;

    private final FortuneBillFactory fortuneBillFactory;

    private final FortuneAccountFactory fortuneAccountFactory;

    private final ApplicationScopeBo applicationScopeBo;

    private final FortuneTagRelationService fortuneTagRelationService;

    private final FortunePayeeFactory fortunePayeeFactory;

    private final FortuneCategoryRelationService fortuneCategoryRelationService;

    public IPage<FortuneBillEntity> getPage(FortuneBillQuery query) {
        return fortuneBillRepository.page(query.toPage(), query.addQueryCondition());
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneBillAddCommand addCommand) {
        // 参数校验前置
        Objects.requireNonNull(addCommand, "addCommand不能为空");
        if (CollectionUtils.isEmpty(addCommand.getCategoryList())) {
            throw new IllegalArgumentException("categoryList不能为空");
        }

        // 主模型操作
        FortuneBillModel fortuneBillModel = fortuneBillFactory.create();
        fortuneBillModel.loadAddCommand(addCommand);
        fortuneBillModel.checkBookId(fortuneBillModel.getBookId());

        // 收款人校验提前
        if (Objects.nonNull(addCommand.getPayeeId())) {
            FortunePayeeModel payee = fortunePayeeFactory.loadById(addCommand.getPayeeId());
            fortuneBillModel.checkPayeeExist(payee);
        }

        // 金额计算优化
        BigDecimal amount = addCommand.getCategoryList().stream()
                .map(Pair::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 使用枚举类型直接比较
        BillTypeEnum billType = BillTypeEnum.getByValue(addCommand.getBillType());
        if (billType == BillTypeEnum.EXPENSE || billType == BillTypeEnum.INCOME) {
            fortuneBillModel.setAmount(amount);
            fortuneBillModel.setConvertedAmount(amount);
        }

        // 资金操作
        this.confirmBalance(fortuneBillModel);

        // 持久化主记录
        fortuneBillModel.insert();
        Long billId = fortuneBillModel.getBillId();  // 提前获取ID

        // 批量标签处理
        processTagRelations(addCommand.getTagIdList(), billId);

        // 批量分类处理
        processCategoryRelations(addCommand.getCategoryList(), billId);
    }

    /**
     * 批量处理标签关联
     */
    private void processTagRelations(List<Long> tagIds, Long billId) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<FortuneTagRelationAddCommand> commands = tagIds.stream()
                .map(tagId -> new FortuneTagRelationAddCommand(billId, tagId))
                .collect(Collectors.toList());
        fortuneTagRelationService.batchAdd(commands);  // 需要实现批量插入方法
    }

    /**
     * 批量处理分类关联
     */
    private void processCategoryRelations(List<Pair<Long, BigDecimal>> categories, Long billId) {
        List<FortuneCategoryRelationAddCommand> commands = categories.stream()
                .map(pair -> new FortuneCategoryRelationAddCommand(billId, pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
        fortuneCategoryRelationService.batchAdd(commands);  // 需要实现批量插入方法
    }

    // 修改的逻辑，删除旧的，新增一条新纪录
    @Transactional(rollbackFor = Exception.class)
    public void modify(FortuneBillModifyCommand modifyCommand) {
        this.remove(modifyCommand.getBookId(), modifyCommand.getBillId());
        this.add(modifyCommand);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        // 账户金额回滚
        this.refundBalance(fortuneBillModel);
        // 删除标签
        fortuneTagRelationService.removeByBillId(billId);
        // 删除分类
        fortuneCategoryRelationService.removeByBillId(billId);
        fortuneBillModel.deleteById();
    }

    /**
     * 确认余额
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmBalance(FortuneBillModel fortuneBillModel) {
        if (!fortuneBillModel.getConfirm() || Objects.isNull(fortuneBillModel.getAccountId())) {
            return;
        }
        // 加载源账户并获取账单类型
        FortuneAccountModel sourceAccount = fortuneAccountFactory.loadById(fortuneBillModel.getAccountId());
        BillTypeEnum billType = BillTypeEnum.getByValue(fortuneBillModel.getBillType());

        // 根据账单类型处理账户余额
        switch (billType) {
            case INCOME, PROFIT, ADJUST:
                this.adjustBalance(sourceAccount, fortuneBillModel.getAmount(), BalanceOperationEnum.ADD);
                break;
            case EXPENSE:
            case LOSS:
                this.adjustBalance(sourceAccount, fortuneBillModel.getAmount(), BalanceOperationEnum.SUBTRACT);
                break;
            case TRANSFER:
                this.handleTransferOperation(sourceAccount, fortuneBillModel);
                break;
            case null, default:
                log.warn("Unsupported bill type: {}", billType);
                break;
        }

        // 更新源账户信息
        sourceAccount.updateById();
    }

    /**
     * 辅助方法：调整账户余额
     */
    private void adjustBalance(FortuneAccountModel account, BigDecimal amount, BalanceOperationEnum operation) {
        if (account == null || amount == null) return;

        BigDecimal newBalance = operation == BalanceOperationEnum.ADD ?
                account.getBalance().add(amount) :
                account.getBalance().subtract(amount);
        account.setBalance(newBalance);
    }

    /**
     * 辅助方法：处理转账操作
     */
    private void handleTransferOperation(FortuneAccountModel sourceAccount, FortuneBillModel bill) {
        // 验证目标账户是否存在
        if (Objects.isNull(bill.getToAccountId())) {
            log.error("Missing target account for transfer operation");
            return;
        }

        // 调整源账户余额
        adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.SUBTRACT);

        // 加载目标账户并转换金额
        FortuneAccountModel targetAccount = fortuneAccountFactory.loadById(bill.getToAccountId());
        BigDecimal convertedAmount = convertCurrency(
                bill.getAmount(),
                sourceAccount.getCurrencyCode(),
                targetAccount.getCurrencyCode(),
                applicationScopeBo.getCurrencyTemplateBoList()
        );

        // 调整目标账户余额
        adjustBalance(targetAccount, convertedAmount, BalanceOperationEnum.ADD);

        // 更新目标账户并记录转换金额
        targetAccount.updateById();
        bill.setConvertedAmount(convertedAmount);
    }

    /**
     * 辅助方法：货币转换
     */
    public BigDecimal convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency, List<CurrencyTemplateBo> rates) {
        if (sourceCurrency.equals(targetCurrency)) {
            return amount;
        }

        // 获取 sourceCurrency 对人民币的汇率
        BigDecimal rateSourceToRMB = rates.stream()
                .filter(rate -> rate.getCurrencyName().equals(sourceCurrency))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.RATE_NOT_FOUND, sourceCurrency, " -> 人民币"));

        // 获取 targetCurrency 对人民币的汇率
        BigDecimal rateTargetToRMB = rates.stream()
                .filter(rate -> rate.getCurrencyName().equals(targetCurrency))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.RATE_NOT_FOUND, "人民币", targetCurrency));

        // 计算目标货币金额
        return amount.multiply(rateSourceToRMB)
                .divide(rateTargetToRMB, 10, RoundingMode.HALF_UP);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(FortuneBillModel bill) {
        // 验证退款基本条件
        if (!isValidRefundRequest(bill)) {
            log.warn("Invalid refund request for bill: {}", bill.getBillId());
            return;
        }

        // 加载账户并处理退款
        FortuneAccountModel sourceAccount = fortuneAccountFactory.loadById(bill.getAccountId());
        BillTypeEnum billType = BillTypeEnum.getByValue(bill.getBillType());
        processRefundByType(sourceAccount, bill, billType);
    }

    /**
     * 条件验证方法
     */
    private boolean isValidRefundRequest(FortuneBillModel bill) {
        return bill.getConfirm() && Objects.nonNull(bill.getAccountId())
                && Objects.nonNull(bill.getAmount()) && bill.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 退款处理核心逻辑
     */
    private void processRefundByType(FortuneAccountModel sourceAccount, FortuneBillModel bill, BillTypeEnum billType) {
        switch (billType) {
            case INCOME, PROFIT, ADJUST:
                this.adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.SUBTRACT);
                break;
            case EXPENSE, LOSS:
                this.adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.ADD);
                break;
            case TRANSFER:
                this.handleTransferRefund(sourceAccount, bill);
                break;
            default:
                log.warn("Unsupported refund type: {}", billType);
                break;
        }
        sourceAccount.updateById();
    }

    /**
     * 处理转账退款
     */
    private void handleTransferRefund(FortuneAccountModel sourceAccount, FortuneBillModel bill) {
        // 验证转账必要参数
        if (Objects.isNull(bill.getToAccountId()) || Objects.isNull(bill.getConvertedAmount())) {
            throw new ApiException(ErrorCode.Business.BILL_TRANSFER_PARAMETER_ERROR);
        }

        // 逆向处理源账户
        adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.ADD);

        // 处理目标账户
        FortuneAccountModel targetAccount = fortuneAccountFactory.loadById(bill.getToAccountId());
        adjustBalance(targetAccount, bill.getConvertedAmount(), BalanceOperationEnum.SUBTRACT);
        targetAccount.updateById();
    }
}
