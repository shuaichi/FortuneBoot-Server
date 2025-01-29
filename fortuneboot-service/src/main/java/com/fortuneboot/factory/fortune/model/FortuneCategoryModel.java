package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneCategoryAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 分类
 *
 * @author zhangchi118
 * @date 2025/1/10 19:50
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneCategoryModel extends FortuneCategoryEntity {

    private FortuneCategoryRepository fortuneCategoryRepository;

    public FortuneCategoryModel(FortuneCategoryRepository repository) {
        this.fortuneCategoryRepository = repository;
    }

    public FortuneCategoryModel(FortuneCategoryEntity entity, FortuneCategoryRepository repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneCategoryRepository = repository;
    }

    public void loadAddCommand(FortuneCategoryAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "categoryId");
        }
    }

    public void loadModifyCommand(FortuneCategoryModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkHeight() {
        // TODO 检查分类高度
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(this.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.CATEGORY_NOT_MATCH_BOOK);
        }
    }
}
