package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneTagAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
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
public class FortuneTagModel extends FortuneTagEntity {

    private FortuneTagRepo fortuneTagRepo;

    public FortuneTagModel(FortuneTagRepo repository) {
        this.fortuneTagRepo = repository;
    }

    public FortuneTagModel(FortuneTagEntity entity, FortuneTagRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneTagRepo = repository;
    }

    public void loadAddCommand(FortuneTagAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "tagId");
        }
    }

    public void checkTagExist() {
        FortuneTagEntity existTag = fortuneTagRepo.getByBookIdAndName(this.getBookId(), this.getTagName());
        if (Objects.nonNull(existTag) && !Objects.equals(existTag.getTagId(), this.getTagId())) {
            ErrorCode.Business business;
            if (existTag.getRecycleBin()) {
                business = ErrorCode.Business.TAG_ADD_EXIST_IN_RECYCLE_BIN;
            } else {
                business = ErrorCode.Business.TAG_ADD_EXIST;
            }
            throw new ApiException(business, this.getTagName());
        }
    }

    public void loadModifyCommand(FortuneTagModifyCommand command) {
        if (Objects.isNull(command)) {
            return;
        }
        this.loadAddCommand(command);
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(this.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.TAG_NOT_MATCH_BOOK);
        }
    }

    public void checkHeight() {
        Long parentId = this.getParentId();
        int height = 1;
        while (parentId != -1) {
            if (height > 3) {
                throw new ApiException(ErrorCode.Business.TAG_HEIGHT_EXCEEDS_THREE);
            }
            FortuneTagEntity parent = fortuneTagRepo.getById(parentId);
            parentId = parent.getParentId();
            height++;
        }
    }

    public void checkParentId(Long parentId) {
        if (!Objects.equals(this.getParentId(), parentId)) {
            throw new ApiException(ErrorCode.Business.TAG_NOT_MATCH_BOOK);
        }
    }

    public void checkParentInRecycleBin() {
        Long parentId = this.getParentId();
        while (parentId != -1) {
            FortuneTagEntity parent = fortuneTagRepo.getById(parentId);
            if (parent.getRecycleBin()) {
                throw new ApiException(ErrorCode.Business.TAG_PARENT_IN_RECYCLE, parent.getTagName());
            }
            parentId = parent.getParentId();
        }
    }
}
