package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortunePayeeAddCommand;
import com.fortuneboot.domain.command.fortune.FortunePayeeModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.query.fortune.FortunePayeeQuery;
import com.fortuneboot.factory.fortune.FortunePayeeFactory;
import com.fortuneboot.factory.fortune.model.FortunePayeeModel;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import com.fortuneboot.repository.fortune.FortunePayeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final FortuneBillRepository fortuneBillRepository;

    public IPage<FortunePayeeEntity> getPage(FortunePayeeQuery query) {
        return fortunePayeeRepository.page(query.toPage(),query.addQueryCondition());
    }

    public List<FortunePayeeEntity> getEnableList(Long bookId, Integer billType) {
        BillTypeEnum billTypeEnum = BillTypeEnum.getByValue(billType);
        switch (billTypeEnum) {
            case INCOME, EXPENSE -> {
                return fortunePayeeRepository.getEnablePayeeList(bookId, billType);
            }
            case null -> {
                return fortunePayeeRepository.getEnablePayeeList(bookId, null);
            }
            default -> {
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
        Boolean used = fortuneBillRepository.existByPayeeId(payeeId);
        if (used) {
            throw new ApiException(ErrorCode.Business.PAYEE_ALREADY_USED);
        }
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

    public void canExpense(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setCanExpense(Boolean.TRUE);
        fortunePayeeModel.updateById();
    }

    public void cannotExpense(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setCanExpense(Boolean.FALSE);
        fortunePayeeModel.updateById();
    }

    public void canIncome(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setCanIncome(Boolean.TRUE);
        fortunePayeeModel.updateById();
    }

    public void cannotIncome(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setCanIncome(Boolean.FALSE);
        fortunePayeeModel.updateById();
    }

    public void enable(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setEnable(Boolean.TRUE);
        fortunePayeeModel.updateById();
    }

    public void disable(Long bookId, Long payeeId) {
        FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(payeeId);
        fortunePayeeModel.checkBookId(bookId);
        fortunePayeeModel.setEnable(Boolean.FALSE);
        fortunePayeeModel.updateById();
    }
}
