package com.fortuneboot.service.fortune;

import com.fortuneboot.domain.command.fortune.FortunePayeeAddCommand;
import com.fortuneboot.domain.command.fortune.FortunePayeeModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.query.fortune.FortunePayeeQuery;
import com.fortuneboot.factory.fortune.FortunePayeeFactory;
import com.fortuneboot.factory.fortune.model.FortunePayeeModel;
import com.fortuneboot.repository.fortune.FortunePayeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/1/10 17:31
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortunePayeeService {

    private final FortunePayeeRepository fortunePayeeRepository;

    private final FortunePayeeFactory fortunePayeeFactory;

    public List<FortunePayeeEntity> getList(FortunePayeeQuery query) {
        return fortunePayeeRepository.list(query.addQueryCondition());
    }

    public void add(FortunePayeeAddCommand addCommand) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.create();
        fortunePayeeModel.loadAddCommand(addCommand);
        fortunePayeeModel.checkPayeeExist();
        fortunePayeeModel.insert();
    }

    public void modify(FortunePayeeModifyCommand modifyCommand) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(modifyCommand.getPayeeId());
        fortunePayeeModel.loadModifyCommand(modifyCommand);
        fortunePayeeModel.checkPayeeExist();
        fortunePayeeModel.checkBookId(modifyCommand.getBookId());
        fortunePayeeModel.updateById();
    }

    public void moveToRecycleBin(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setRecycleBin(Boolean.TRUE);
        fortunePayeeModel.updateById();
    }

    public void delete(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.deleteById();
    }

    public void putBack(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setRecycleBin(Boolean.FALSE);
        fortunePayeeModel.updateById();
    }
}
