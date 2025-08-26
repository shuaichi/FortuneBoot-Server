package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.fortune.AccountTypeEnum;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountAdjustCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 15:54
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneAccountModel extends FortuneAccountEntity {

    private FortuneAccountRepo fortuneAccountRepo;

    public FortuneAccountModel(FortuneAccountRepo repository) {
        this.fortuneAccountRepo = repository;
    }

    public FortuneAccountModel(FortuneAccountEntity entity, FortuneAccountRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneAccountRepo = repository;
    }

    public void loadAddCommand(FortuneAccountAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "accountId");
        }
    }

    public void loadModifyCommand(FortuneAccountModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkAccountType() {
        AccountTypeEnum accountTypeEnum = AccountTypeEnum.getEnumByValue(this.getAccountType());
        switch (accountTypeEnum) {
            case CURRENT, CREDIT, ASSET, LOAN -> {
            }
            case null, default -> throw new ApiException(ErrorCode.Business.ACCOUNT_TYPE_ERROR);
        }
    }

    public void checkGroupId(Long groupId) {
        if (!Objects.equals(groupId, this.getGroupId())) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_NOT_MATCH_GROUP);
        }
    }

    public void checkCanExpense() {
        if (!this.getCanExpense()) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_CANNOT_EXPENSE, this.getAccountName());
        }
    }

    public void checkCanIncome() {
        if (!this.getCanIncome()) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_CANNOT_INCOME, this.getAccountName());
        }
    }

    public void checkCanTransferIn() {
        if (!this.getCanTransferIn()) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_CANNOT_TRANSFER_IN, this.getAccountName());
        }
    }

    public void checkCanTransferOut() {
        if (!this.getCanTransferOut()) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_CANNOT_TRANSFER_OUT, this.getAccountName());
        }
    }

    public void checkEnable() {
        if (!this.getEnable()) {
            throw new ApiException(ErrorCode.Business.BILL_ACCOUNT_DISABLE, this.getAccountName());
        }
    }

    public FortuneBillAddCommand loadAdjustCommand(FortuneAccountAdjustCommand adjustCommand) {
        BigDecimal balance = adjustCommand.getBalance().subtract(this.getBalance());
        if (BigDecimal.ZERO.compareTo(balance) == 0) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_BALANCE_ADJUST_NOT_MODIFY);
        }
        FortuneBillAddCommand fortuneBill = new FortuneBillAddCommand();
        fortuneBill.setAmount(balance);
        fortuneBill.setConvertedAmount(balance);
        fortuneBill.setBillType(BillTypeEnum.ADJUST.getValue());
        fortuneBill.setAccountId(adjustCommand.getAccountId());
        fortuneBill.setBookId(adjustCommand.getBookId());
        fortuneBill.setTradeTime(adjustCommand.getTradeTime());
        fortuneBill.setTitle(StringUtils.isBlank(adjustCommand.getTitle()) ? "余额调整" : adjustCommand.getTitle());
        fortuneBill.setRemark(adjustCommand.getRemark());
        fortuneBill.setConfirm(Boolean.TRUE);
        fortuneBill.setInclude(Boolean.TRUE);
        return fortuneBill;
    }
}
