package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.fortune.FinanceOrderStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneFinanceOrderModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 单据表模型
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:15
 **/
public class FortuneFinanceOrderModel extends FortuneFinanceOrderEntity {

    private final FortuneFinanceOrderRepo fortuneFinanceOrderRepo;

    public FortuneFinanceOrderModel(FortuneFinanceOrderRepo fortuneFinanceOrderRepo) {
        this.fortuneFinanceOrderRepo = fortuneFinanceOrderRepo;
    }

    public FortuneFinanceOrderModel(FortuneFinanceOrderRepo fortuneFinanceOrderRepo, FortuneFinanceOrderEntity entity) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneFinanceOrderRepo = fortuneFinanceOrderRepo;
    }

    public void loadAddCommand(FortuneFinanceOrderAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "order_id");
            this.setSubmitTime(LocalDateTime.now());
            this.setStatus(FinanceOrderStatusEnum.INIT.getValue());
        }
    }

    public void loadModifyCommand(FortuneFinanceOrderModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkModifyStatus() {
        if (Objects.equals(this.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_CLOSE_CAN_NOT_MODIFY);
        }
    }

    public void checkBillExist(Boolean existed) {
        if (existed) {
            throw new ApiException(ErrorCode.Business.ORDER_BILL_EXISTED_CAN_NOT_REMOVE);
        }
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(this.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.ORDER_BOOK_NOT_MATCH);
        }
    }

    public void checkUsingStatus() {
        if (!Objects.equals(this.getStatus(), FinanceOrderStatusEnum.INIT.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_USING_STATUS_ERROR);
        }
    }
    public void checkCloseStatus() {
        if (!Objects.equals(this.getStatus(), FinanceOrderStatusEnum.USING.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_CLOSE_STATUS_ERROR);
        }
    }

    public void checkReopenStatus() {
        if (!Objects.equals(this.getStatus(), FinanceOrderStatusEnum.CLOSE.getValue())) {
            throw new ApiException(ErrorCode.Business.ORDER_REOPEN_STATUS_ERROR);
        }
    }

}
