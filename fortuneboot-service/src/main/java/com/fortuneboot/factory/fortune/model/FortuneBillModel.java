package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:43
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBillModel extends FortuneBillEntity {

    private final FortuneBillRepository fortuneBillRepository;

    public FortuneBillModel(FortuneBillRepository repository) {
        this.fortuneBillRepository = repository;
    }

    public FortuneBillModel(FortuneBillEntity entity, FortuneBillRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneBillRepository = repository;
    }

    public void loadAddCommand(FortuneBillAddCommand addCommand) {
        if (Objects.nonNull(addCommand)) {
            BeanUtil.copyProperties(addCommand, this,"billId");
        }
    }

    public void loadModifyCommand(FortuneBillModifyCommand modifyCommand){
        if (Objects.isNull(modifyCommand)) {
            return;
        }
        this.loadAddCommand(modifyCommand);
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(bookId,this.getBookId())){
            throw new ApiException(ErrorCode.Business.BILL_NOT_MATCH_BOOK);
        }
    }
}
