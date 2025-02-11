package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.*;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * @author zhangchi118
 * @date 2024/11/29 16:10
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBookModel extends FortuneBookEntity {

    private final FortuneBookRepository fortuneBookRepository;

    public FortuneBookModel(FortuneBookRepository repository) {
        this.fortuneBookRepository = repository;
    }

    public FortuneBookModel(FortuneBookEntity entity, FortuneBookRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneBookRepository = repository;
    }

    public void loadAddCommand(FortuneBookAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "bookId");
        }
    }

    public void loadModifyCommand(FortuneBookModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkDefault(FortuneGroupModel fortuneGroupModel) {
        if (Objects.equals(fortuneGroupModel.getDefaultBookId(), this.getBookId())) {
            throw new ApiException(ErrorCode.Business.BOOK_DEFAULT_CAN_NOT_REMOVE);
        }
    }

    public void checkNotInRecycleBin() {
        if (this.getRecycleBin()) {
            throw new ApiException(ErrorCode.Business.BOOK_PLEASE_MOVE_OUT_RECYCLE_BIN_FIRST);
        }
    }
}
