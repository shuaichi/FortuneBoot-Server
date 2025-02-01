package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneBookAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBookModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.query.fortune.FortuneBookQuery;
import com.fortuneboot.factory.fortune.FortuneBookFactory;
import com.fortuneboot.factory.fortune.FortuneGroupFactory;
import com.fortuneboot.factory.fortune.model.FortuneBookModel;
import com.fortuneboot.repository.fortune.FortuneBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 账本service
 *
 * @author zhangchi118
 * @date 2024/11/29 15:50
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBookService {

    private final FortuneBookFactory fortuneBookFactory;

    private final FortuneBookRepository fortuneBookRepository;

    private final FortuneGroupFactory fortuneGroupFactory;

    public IPage<FortuneBookEntity> getPage(FortuneBookQuery query) {
        return fortuneBookRepository.page(query.toPage(), query.addQueryCondition());
    }

    public FortuneBookModel add(FortuneBookAddCommand bookAddCommand) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.create();
        fortuneBookModel.loadAddCommand(bookAddCommand);
        fortuneBookModel.setEnable(Boolean.TRUE);
        fortuneBookModel.insert();
        return fortuneBookModel;
    }

    public void modify(FortuneBookModifyCommand bookModifyCommand) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookModifyCommand.getBookId());
        fortuneBookModel.loadModifyCommand(bookModifyCommand);
        fortuneBookModel.checkNotInRecycleBin();
        fortuneBookModel.updateById();
    }

    public void remove(Long groupId, Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkGroupId(groupId);
        fortuneBookModel.deleteById();
    }

    public void moveToRecycleBin(Long groupId, Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkGroupId(groupId);
        fortuneBookModel.checkDefault(fortuneGroupFactory.loadById(groupId));
        fortuneBookModel.setRecycleBin(Boolean.TRUE);
        fortuneBookModel.updateById();
    }

    public void putBack(Long groupId, Long bookId) {
        FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(bookId);
        fortuneBookModel.checkGroupId(groupId);
        fortuneBookModel.setRecycleBin(Boolean.FALSE);
        fortuneBookModel.updateById();
    }

    public List<FortuneBookEntity> getByIds(List<Long> bookIdList) {
        return fortuneBookRepository.listByIds(bookIdList);
    }

    public List<FortuneBookEntity> getByGroupId(Long groupId) {
        return fortuneBookRepository.getByGroupId(groupId);
    }
}
