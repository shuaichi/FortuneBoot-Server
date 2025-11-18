package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneCategoryAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
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

    private FortuneCategoryRepo fortuneCategoryRepo;

    public FortuneCategoryModel(FortuneCategoryRepo repository) {
        this.fortuneCategoryRepo = repository;
    }

    public FortuneCategoryModel(FortuneCategoryEntity entity, FortuneCategoryRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneCategoryRepo = repository;
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
        Long pId = this.getParentId();
        int height = 1;
        while (pId != -1) {
            if (height > 3) {
                throw new ApiException(ErrorCode.Business.TAG_HEIGHT_EXCEEDS_THREE);
            }
            FortuneCategoryEntity parent = fortuneCategoryRepo.getById(pId);
            pId = parent.getParentId();
            height++;
        }
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(this.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.CATEGORY_NOT_MATCH_BOOK);
        }
    }

    public void checkParentId(Long parentId) {
        if (!Objects.equals(this.getParentId(), parentId)) {
            throw new ApiException(ErrorCode.Business.CATEGORY_PARENT_NOT_SUPPORT_MODIFY);
        }
    }

    public void checkParentInRecycleBin() {
        Long pId = this.getParentId();
        while (pId != -1) {
            FortuneCategoryEntity parent = fortuneCategoryRepo.getById(pId);
            if (parent.getRecycleBin()) {
                throw new ApiException(ErrorCode.Business.CATEGORY_PARENT_IN_RECYCLE,parent.getCategoryName());
            }
            pId = parent.getParentId();
        }
    }
}
