package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.CategoryTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneCategoryAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.query.fortune.FortuneCategoryQuery;
import com.fortuneboot.domain.vo.fortune.FortuneCategoryVo;
import com.fortuneboot.factory.fortune.FortuneCategoryFactory;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepository;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
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
 * @author zhangchi118
 * @date 2025/1/10 18:33
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneCategoryService {

    private final FortuneCategoryRepository fortuneCategoryRepository;

    private final FortuneCategoryFactory fortuneCategoryFactory;

    private final FortuneCategoryRelationRepository fortuneCategoryRelationRepository;

    public PageDTO<FortuneCategoryVo> getPage(FortuneCategoryQuery query) {
        IPage<FortuneCategoryEntity> page = fortuneCategoryRepository.page(query.toPage(), query.addQueryCondition());
        List<FortuneCategoryVo> records = page.getRecords().stream().map(FortuneCategoryVo::new).toList();
        this.fillChildren(records);
        return new PageDTO<>(records, page.getTotal());
    }

    private void fillChildren(List<FortuneCategoryVo> fortuneCategoryVos) {
        if (CollectionUtils.isEmpty(fortuneCategoryVos)) {
            return;
        }
        List<Long> parentIds = fortuneCategoryVos.stream().map(FortuneCategoryVo::getCategoryId).toList();
        Map<Long, List<FortuneCategoryEntity>> map = fortuneCategoryRepository.getByParentIds(parentIds);
        List<FortuneCategoryVo> childrenVo = new ArrayList<>();
        for (FortuneCategoryVo tagVo : fortuneCategoryVos) {
            List<FortuneCategoryEntity> childrenEntity = map.get(tagVo.getCategoryId());
            if (CollectionUtils.isEmpty(childrenEntity)) {
                continue;
            }
            List<FortuneCategoryVo> list = childrenEntity.stream().map(FortuneCategoryVo::new).toList();
            list.forEach(tagVo::addChild);
            childrenVo.addAll(list);
        }
        this.fillChildren(childrenVo);
    }

    public List<FortuneCategoryEntity> getEnableCategoryList(Long bookId, Integer billType) {
        CategoryTypeEnum categoryTypeEnum = CategoryTypeEnum.getByValue(billType);
        switch (categoryTypeEnum) {
            case INCOME, EXPENSE -> {
                return fortuneCategoryRepository.getEnableCategoryList(bookId, billType);
            }
            case null, default -> {
                return Collections.emptyList();
            }
        }
    }

    public FortuneCategoryModel add(FortuneCategoryAddCommand addCommand) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.create();
        fortuneCategoryModel.loadAddCommand(addCommand);
        fortuneCategoryModel.checkHeight();
        fortuneCategoryModel.insert();
        return fortuneCategoryModel;
    }

    public void modify(FortuneCategoryModifyCommand modifyCommand) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(modifyCommand.getCategoryId());
        fortuneCategoryModel.loadModifyCommand(modifyCommand);
        fortuneCategoryModel.checkBookId(modifyCommand.getBookId());
        fortuneCategoryModel.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.setRecycleBin(Boolean.TRUE);
        fortuneCategoryModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long categoryId) {
        Boolean used = fortuneCategoryRelationRepository.existByCategoryId(categoryId);
        if (used) {
            throw new ApiException(ErrorCode.Business.CATEGORY_ALREADY_USED);
        }
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.deleteById();
        List<FortuneCategoryEntity> children = fortuneCategoryRepository.getByParentId(categoryId);
        for (FortuneCategoryEntity child : children) {
            this.remove(bookId, child.getCategoryId());
        }
    }

    public void putBack(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.setRecycleBin(Boolean.FALSE);
        // TODO 校验父级是否在回收站
        fortuneCategoryModel.updateById();
    }
}
