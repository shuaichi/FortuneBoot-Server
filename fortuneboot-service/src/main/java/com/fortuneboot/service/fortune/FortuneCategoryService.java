package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneCategoryAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneCategoryEntity;
import com.fortuneboot.domain.query.fortune.FortuneCategoryQuery;
import com.fortuneboot.factory.fortune.FortuneCategoryFactory;
import com.fortuneboot.factory.fortune.model.FortuneCategoryModel;
import com.fortuneboot.repository.fortune.FortuneCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<FortuneCategoryEntity> getList(FortuneCategoryQuery query) {
        return fortuneCategoryRepository.list(query.addQueryCondition());
    }

    public void add(FortuneCategoryAddCommand addCommand) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.create();
        fortuneCategoryModel.loadAddCommand(addCommand);
        fortuneCategoryModel.checkHeight();
        fortuneCategoryModel.insert();
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
        // TODO 子级一起移入回收站
        fortuneCategoryModel.updateById();
    }

    public void remove(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        // TODO 子级一起删除
        fortuneCategoryModel.deleteById();
    }

    public void putBack(Long bookId, Long categoryId) {
        FortuneCategoryModel fortuneCategoryModel = fortuneCategoryFactory.loadById(categoryId);
        fortuneCategoryModel.checkBookId(bookId);
        fortuneCategoryModel.setRecycleBin(Boolean.FALSE);
        // TODO 校验父级是否在回收站
        fortuneCategoryModel.updateById();
    }
}
