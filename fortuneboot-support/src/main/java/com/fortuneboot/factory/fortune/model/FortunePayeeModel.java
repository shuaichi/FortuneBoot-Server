package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortunePayeeAddCommand;
import com.fortuneboot.domain.command.fortune.FortunePayeeModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 账本标签模型
 *
 * @author zhangchi118
 * @date 2024/12/11 16:17
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortunePayeeModel extends FortunePayeeEntity {

    private FortunePayeeRepo fortunePayeeRepo;

    public FortunePayeeModel(FortunePayeeRepo repository) {
        this.fortunePayeeRepo = repository;
    }

    public FortunePayeeModel(FortunePayeeEntity entity, FortunePayeeRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortunePayeeRepo = repository;
    }

    public void loadAddCommand(FortunePayeeAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "payeeId");
        }
    }

    public void loadModifyCommand(FortunePayeeModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(this.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.PAYEE_NOT_MATCH_BOOK);
        }
    }

    public void checkPayeeExist() {
        FortunePayeeEntity existTag = fortunePayeeRepo.getByBookIdAndName(this.getBookId(), this.getPayeeName());
        if (Objects.nonNull(existTag) && !Objects.equals(existTag.getPayeeId(), this.getPayeeId())) {
            ErrorCode.Business business;
            if (existTag.getRecycleBin()) {
                business = ErrorCode.Business.PAYEE_ADD_EXIST_IN_RECYCLE_BIN;
            } else {
                business = ErrorCode.Business.PAYEE_ADD_EXIST;
            }
            throw new ApiException(business, this.getPayeeName());
        }
    }
}
