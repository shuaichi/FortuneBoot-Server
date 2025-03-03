package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.fortune.AccountTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 15:54
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneAccountModel extends FortuneAccountEntity {

    private FortuneAccountRepository fortuneAccountRepository;

    public FortuneAccountModel(FortuneAccountRepository repository) {
        this.fortuneAccountRepository = repository;
    }

    public FortuneAccountModel(FortuneAccountEntity entity, FortuneAccountRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneAccountRepository = repository;
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
}
