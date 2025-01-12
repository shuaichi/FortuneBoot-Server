package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortuneTagAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;
import com.fortuneboot.domain.query.fortune.FortuneTagQuery;
import com.fortuneboot.factory.fortune.FortuneTagFactory;
import com.fortuneboot.factory.fortune.model.FortuneTagModel;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<FortuneTagEntity> getList(FortuneTagQuery query) {
        return fortuneTagRepository.list(query.addQueryCondition());
    }

    public void add(FortuneTagAddCommand addCommand) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.create();
        fortuneTagModel.loadAddCommand(addCommand);
        fortuneTagModel.checkTagExist();
        fortuneTagModel.checkHeight();
        fortuneTagModel.insert();
    }

    public void modify(FortuneTagModifyCommand modifyCommand) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(modifyCommand.getTagId());
        fortuneTagModel.loadModifyCommand(modifyCommand);
        fortuneTagModel.checkTagExist();
        fortuneTagModel.checkBookId(modifyCommand.getBookId());
        fortuneTagModel.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setRecycleBin(Boolean.TRUE);
        // TODO 子级一起移入回收站
        fortuneTagModel.updateById();
    }

    public void remove(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        // TODO 子级一起删除
        fortuneTagModel.deleteById();
    }


    public void putBack(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.setRecycleBin(Boolean.FALSE);
        // TODO 校验父级是否在回收站
        fortuneTagModel.updateById();
    }

    public void modifyCanExpense(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.modifyCanExpense();
        fortuneTagModel.updateById();
    }

    public void modifyCanIncome(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.modifyCanIncome();
        fortuneTagModel.updateById();
    }

    public void modifyCanTransfer( Long bookId,  Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.modifyCanTransfer();
        fortuneTagModel.updateById();
    }

    public void modifyEnable(Long bookId, Long tagId) {
        FortuneTagModel fortuneTagModel = fortuneTagFactory.loadById(tagId);
        fortuneTagModel.checkBookId(bookId);
        fortuneTagModel.modifyEnable();
        fortuneTagModel.updateById();
    }

}
