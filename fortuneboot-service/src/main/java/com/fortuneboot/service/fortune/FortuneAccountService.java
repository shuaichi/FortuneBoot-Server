package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.query.fortune.FortuneAccountQuery;
import com.fortuneboot.factory.fortune.FortuneAccountFactory;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 22:48
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneAccountService {

    private final FortuneAccountRepository fortuneAccountRepository;

    private final FortuneAccountFactory fortuneAccountFactory;

    public IPage<FortuneAccountEntity> getPage(FortuneAccountQuery query) {
        return fortuneAccountRepository.page(query.toPage(), query.addQueryCondition());
    }

    public List<FortuneAccountEntity> getEnableList(Long groupId) {
        return fortuneAccountRepository.getEnableList(groupId);
    }

    public void add(FortuneAccountAddCommand addCommand) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.create();
        fortuneAccountModel.loadAddCommand(addCommand);
        fortuneAccountModel.checkAccountType();
        fortuneAccountModel.insert();
    }

    public void modify(FortuneAccountModifyCommand modifyCommand) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(modifyCommand.getAccountId());
        fortuneAccountModel.loadModifyCommand(modifyCommand);
        fortuneAccountModel.checkAccountType();
        fortuneAccountModel.updateById();
    }

    public void moveToRecycleBin(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setRecycleBin(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void remove(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.deleteById();
    }

    public void putBack(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setRecycleBin(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canExpense( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanExpense(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotExpense( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanExpense(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canIncome( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanIncome(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotIncome( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanIncome(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canTransferOut( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferOut(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotTransferOut( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferOut(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canTransferIn( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferIn(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotTransferIn( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferIn(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void includeAccount( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setInclude(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void excludeAccount( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setInclude(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void enable( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setEnable(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void disable( Long groupId,  Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setEnable(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

}
