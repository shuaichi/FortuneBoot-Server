package com.fortuneboot.factory.fortune.model;

import cn.hutool.core.bean.BeanUtil;
import com.fortuneboot.common.enums.fortune.GoodsKeeperStatusEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneGoodsKeeperAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneGoodsKeeperEntity;
import com.fortuneboot.repository.fortune.FortuneGoodsKeeperRepo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 归物
 *
 * @author zhangchi118
 * @date 2025/5/6 11:20
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FortuneGoodsKeeperModel extends FortuneGoodsKeeperEntity {

    private FortuneGoodsKeeperRepo fortuneGoodsKeeperRepo;

    public FortuneGoodsKeeperModel(FortuneGoodsKeeperRepo repository) {
        this.fortuneGoodsKeeperRepo = repository;
    }

    public FortuneGoodsKeeperModel(FortuneGoodsKeeperEntity entity, FortuneGoodsKeeperRepo repository) {
        this.fortuneGoodsKeeperRepo = repository;
        if (Objects.nonNull(entity)) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    public void loadAddCommand(FortuneGoodsKeeperAddCommand addCommand) {
        if (Objects.nonNull(addCommand)) {
            BeanUtil.copyProperties(addCommand, this, "goodsKeeperId");
        }
    }

    public void loadModifyCommand(FortuneGoodsKeeperAddCommand modifyCommand) {
        if (Objects.isNull(modifyCommand)) {
            return;
        }
        this.loadAddCommand(modifyCommand);
    }


    public void checkBookId(Long bookId) {
        if (!Objects.equals(bookId, this.getBookId())) {
            throw new ApiException(ErrorCode.Business.GOODS_KEEPER_BOOK_NOT_MATCH);
        }
    }

    public void checkStatus(Integer status) {
        if (!GoodsKeeperStatusEnum.contains(status)) {
            throw new ApiException(ErrorCode.Business.GOODS_KEEPER_STATUS_NOT_MATCH);
        }
    }
    public void checkRetiredDate(Integer status, LocalDate retiredDate) {
        if (!GoodsKeeperStatusEnum.ACTIVE.getValue().equals(status) && Objects.isNull(retiredDate)) {
            throw new ApiException(ErrorCode.Business.GOODS_KEEPER_STATUS_NEED_RETIRED_DATE);
        }

    }
}
