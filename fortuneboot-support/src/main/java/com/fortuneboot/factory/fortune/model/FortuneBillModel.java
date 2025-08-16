package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:43
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneBillModel extends FortuneBillEntity {

    private final FortuneBillRepo fortuneBillRepo;

    public FortuneBillModel(FortuneBillRepo repository) {
        this.fortuneBillRepo = repository;
    }

    public FortuneBillModel(FortuneBillEntity entity, FortuneBillRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneBillRepo = repository;
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

    public void checkPayeeExist(FortunePayeeModel fortunePayeeModel) {
        if (Objects.isNull(fortunePayeeModel)) {
            throw new ApiException(ErrorCode.Business.BILL_PAYEE_NOT_EXIST);
        }
    }

    public void checkPayeeEnable(FortunePayeeModel payee) {
        if (!payee.getEnable()){
            throw new ApiException(ErrorCode.Business.BILL_PAYEE_DISABLE,payee.getPayeeName());
        }
    }

    public void checkTagListEnable(List<FortuneTagModel> fortuneTagModels) {
        List<String> disables = fortuneTagModels.stream().filter(model -> !model.getEnable()).map(FortuneTagModel::getTagName).toList();
        if (CollectionUtils.isNotEmpty(disables)) {
            throw new ApiException(ErrorCode.Business.BILL_TAG_DISABLE, disables.toString());
        }
    }

    public void checkCategoryListEnable(List<FortuneCategoryModel> fortuneCategoryModels) {
        List<String> disables = fortuneCategoryModels.stream().filter(model -> !model.getEnable()).map(FortuneCategoryModel::getCategoryName).toList();
        if (CollectionUtils.isNotEmpty(disables)) {
            throw new ApiException(ErrorCode.Business.BILL_CATEGORY_DISABLE, disables.toString());
        }
    }

    public void checkAccountEnable(FortuneAccountModel account) {
        if (!account.getEnable()){
            throw new ApiException(ErrorCode.Business.BILL_ACCOUNT_DISABLE, account.getAccountName());
        }
    }

}
