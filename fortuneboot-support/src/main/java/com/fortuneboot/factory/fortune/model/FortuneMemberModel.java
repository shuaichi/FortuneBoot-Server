package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneMemberAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneMemberModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneMemberEntity;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 *
 * @author zhangchi118
 * @date 2026/3/19 09:32
 **/

@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneMemberModel extends FortuneMemberEntity {

    private FortuneMemberRepo fortuneMemberRepo;

    public FortuneMemberModel(FortuneMemberRepo repository) {
        this.fortuneMemberRepo = repository;
    }

    public FortuneMemberModel(FortuneMemberEntity entity, FortuneMemberRepo repository) {
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
        this.fortuneMemberRepo = repository;
    }

    public void loadAddCommand(FortuneMemberAddCommand command) {
        if (Objects.nonNull(command)) {
            BeanUtil.copyProperties(command, this, "memberId");
        }
    }

    public void loadModifyCommand(FortuneMemberModifyCommand command) {
        if (Objects.nonNull(command)) {
            this.loadAddCommand(command);
        }
    }

    public void checkMemberExist() {
        FortuneMemberEntity exist = fortuneMemberRepo.getByBookIdAndName(this.getBookId(), this.getMemberName());
        if (Objects.nonNull(exist) && !Objects.equals(exist.getMemberId(), this.getMemberId())) {
            // 借用之前异常类的设计，这里复用 TAG_ADD_EXIST (也可以在 ErrorCode 里加新的成员枚举)
            throw new ApiException(ErrorCode.Business.TAG_ADD_EXIST, this.getMemberName());
        }
    }

    public void checkBookId(Long bookId) {
        if (!Objects.equals(this.getBookId(), bookId)) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION);
        }
    }
}