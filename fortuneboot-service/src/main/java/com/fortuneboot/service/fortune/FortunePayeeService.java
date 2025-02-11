package com.fortuneboot.service.fortune;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.command.fortune.FortunePayeeAddCommand;
import com.fortuneboot.domain.command.fortune.FortunePayeeModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.query.fortune.FortunePayeeQuery;
import com.fortuneboot.factory.fortune.FortunePayeeFactory;
import com.fortuneboot.factory.fortune.model.FortunePayeeModel;
import com.fortuneboot.repository.fortune.FortunePayeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    public List<FortunePayeeEntity> getEnablePayeeList(Long bookId, Integer billType) {
        BillTypeEnum billTypeEnum = BillTypeEnum.getByValue(billType);
        switch (billTypeEnum){
            case INCOME, EXPENSE-> {
                return fortunePayeeRepository.getEnablePayeeList(bookId,billType);
            }
            case null, default -> {
                return Collections.emptyList();
            }
        }
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

    public void remove(Long bookId, Long payeeId) {
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

    public void modifyCanExpense(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setCanExpense(!fortunePayeeModel.getCanExpense());
        fortunePayeeModel.updateById();
    }

    public void modifyCanIncome(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setCanIncome(!fortunePayeeModel.getCanIncome());
        fortunePayeeModel.updateById();
    }

    public void modifyEnable(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setEnable(!fortunePayeeModel.getEnable());
        fortunePayeeModel.updateById();
    }
}
