package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneTagAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.domain.vo.fortune.FortuneTagVo;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepository;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 账本标签服务
 *
 * @author zhangchi118
 * @date 2024/12/11 16:13
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneTagService {

    private final FortuneTagRepository fortuneTagRepository;

    private final FortuneTagFactory fortuneTagFactory;

    private final FortuneTagRelationRepository fortuneTagRelationRepository;

    public PageDTO<FortuneTagVo> getPage(FortuneTagQuery query) {
        IPage<FortuneTagEntity> page = fortuneTagRepository.page(query.toPage(), query.addQueryCondition().eq(FortuneTagEntity::getParentId, -1L));
        List<FortuneTagVo> records = page.getRecords().stream().map(FortuneTagVo::new).toList();
        this.fillChildren(records);
        return new PageDTO<>(records, page.getTotal());
    }

    private void fillChildren(List<FortuneTagVo> fortuneTagVos) {
        if (CollectionUtils.isEmpty(fortuneTagVos)) {
            return;
        }
        List<Long> parentIds = fortuneTagVos.stream().map(FortuneTagVo::getTagId).toList();
        Map<Long, List<FortuneTagEntity>> map = fortuneTagRepository.getByParentIds(parentIds);
        List<FortuneTagVo> childrenVo = new ArrayList<>();
        for (FortuneTagVo tagVo : fortuneTagVos) {
            List<FortuneTagEntity> childrenEntity = map.get(tagVo.getTagId());
            if (CollectionUtils.isEmpty(childrenEntity)) {
                continue;
            }
            List<FortuneTagVo> list = childrenEntity.stream().map(FortuneTagVo::new).toList();
            list.forEach(tagVo::addChild);
            childrenVo.addAll(list);
        }
        this.fillChildren(childrenVo);
    }

    public List<FortuneTagEntity> getList(FortuneTagQuery query) {
        return fortuneTagRepository.list(query.addQueryCondition());
    }


    public List<FortuneTagEntity> getEnableList(Long bookId, Integer billType) {
        BillTypeEnum billTypeEnum = BillTypeEnum.getByValue(billType);
        switch (billTypeEnum) {
            case EXPENSE, INCOME, TRANSFER -> {
                return fortuneTagRepository.getEnableTagList(bookId, billType);
            }
            case null, default -> {
                return Collections.emptyList();
            }
        }
    }

    public FortuneTagModel add(FortuneTagAddCommand addCommand) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.create();
        fortuneTagModel.loadAddCommand(addCommand);
        fortuneTagModel.checkTagExist();
        fortuneTagModel.checkHeight();
        fortuneTagModel.insert();
        return fortuneTagModel;
    }

    public void modify(FortuneTagModifyCommand modifyCommand) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(modifyCommand.getTagId());
        fortuneTagModel.loadModifyCommand(modifyCommand);
        fortuneTagModel.checkTagExist();
        fortuneTagModel.checkBookId(modifyCommand.getBookId());
        fortuneTagModel.checkParentId(modifyCommand.getParentId());
        fortuneTagModel.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setRecycleBin(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long tagId) {
        Boolean used = fortuneTagRelationRepository.existByTagId(tagId);
        if (used) {
            throw new ApiException(ErrorCode.Business.TAG_ALREADY_USED);
        }
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        // TODO 子级一起删除
        fortuneTagModel.deleteById();
        // 递归删除子级标签
        List<FortuneTagEntity> children = fortuneTagRepository.getByParentId(tagId);
        for (FortuneTagEntity child : children) {
            this.remove(bookId, child.getTagId());
        }
    }


    public void putBack(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setRecycleBin(Boolean.FALSE);
        // TODO 校验父级是否在回收站
        fortuneTagModel.updateById();
    }

    public void canExpense(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanExpense(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void cannotExpense(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanExpense(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void canIncome(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanIncome(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void cannotIncome(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanIncome(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void canTransfer(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanTransfer(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void cannotTransfer(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setCanTransfer(Boolean.FALSE);
        fortuneTagModel.updateById();
    }

    public void enable(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setEnable(Boolean.TRUE);
        fortuneTagModel.updateById();
    }

    public void disable(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setEnable(Boolean.FALSE);
        fortuneTagModel.updateById();
    }
}
