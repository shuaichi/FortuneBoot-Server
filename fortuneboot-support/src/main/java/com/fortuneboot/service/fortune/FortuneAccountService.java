package com.fortuneboot.service.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountAdjustCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.query.fortune.FortuneAccountQuery;
import com.fortuneboot.domain.vo.fortune.include.FortuneAssetsLiabilitiesVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;
import com.fortuneboot.factory.fortune.factory.FortuneAccountFactory;
import com.fortuneboot.factory.fortune.factory.FortuneGroupFactory;
import com.fortuneboot.factory.fortune.model.FortuneAccountModel;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final FortuneBillRepository fortuneBillRepository;
    private final FortuneBillService fortuneBillService;
    private final FortuneGroupFactory fortuneGroupFactory;

    public IPage<FortuneAccountEntity> getPage(FortuneAccountQuery query) {
        return fortuneAccountRepository.page(query.toPage(), query.addQueryCondition());
    }

    public List<FortuneAccountEntity> getEnableAccountList(Long groupId) {
        return fortuneAccountRepository.getEnableAccountList(groupId);
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
        Boolean exist = fortuneBillRepository.existByAccount(accountId);
        if (exist) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_USED_CANNOT_REMOVE, fortuneAccountModel.getAccountName());
        }
        fortuneAccountModel.deleteById();
    }

    public void putBack(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setRecycleBin(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canExpense(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanExpense(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotExpense(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanExpense(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canIncome(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanIncome(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotIncome(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanIncome(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canTransferOut(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferOut(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotTransferOut(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferOut(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void canTransferIn(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferIn(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void cannotTransferIn(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setCanTransferIn(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void includeAccount(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setInclude(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void excludeAccount(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setInclude(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public void enable(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setEnable(Boolean.TRUE);
        fortuneAccountModel.updateById();
    }

    public void disable(Long groupId, Long accountId) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(accountId);
        fortuneAccountModel.checkGroupId(groupId);
        fortuneAccountModel.setEnable(Boolean.FALSE);
        fortuneAccountModel.updateById();
    }

    public List<FortunePieVo> getTotalAssets(Long groupId) {
        return fortuneAccountRepository.getTotalAssets(groupId);
    }

    public List<FortunePieVo> getTotalLiabilities(Long groupId) {
        return fortuneAccountRepository.getTotalLiabilities(groupId);
    }

    public FortuneAssetsLiabilitiesVo getFortuneAssetsLiabilities(Long groupId) {
        return fortuneAccountRepository.getFortuneAssetsLiabilities(groupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void balanceAdjust(FortuneAccountAdjustCommand adjustCommand) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(adjustCommand.getAccountId());
        fortuneBillService.add(this.initBillAddCommand(adjustCommand, fortuneAccountModel));
        fortuneAccountModel.setBalance(adjustCommand.getBalance());
        fortuneAccountModel.updateById();
    }

    private FortuneBillAddCommand initBillAddCommand(FortuneAccountAdjustCommand adjustCommand, FortuneAccountModel fortuneAccountModel) {
        BigDecimal balance = adjustCommand.getBalance().subtract(fortuneAccountModel.getBalance());
        if (BigDecimal.ZERO.compareTo(balance) == 0) {
            throw new ApiException(ErrorCode.Business.ACCOUNT_BALANCE_ADJUST_NOT_MODIFY);
        }
        FortuneBillAddCommand fortuneBill = new FortuneBillAddCommand();
        fortuneBill.setAmount(balance);
        fortuneBill.setConvertedAmount(balance);
        fortuneBill.setBillType(BillTypeEnum.ADJUST.getValue());
        fortuneBill.setAccountId(adjustCommand.getAccountId());
        fortuneBill.setBookId(adjustCommand.getBookId());
        fortuneBill.setTradeTime(adjustCommand.getTradeTime());
        fortuneBill.setTitle(StringUtils.isBlank(adjustCommand.getTitle()) ? "余额调整" : adjustCommand.getTitle());
        fortuneBill.setRemark(adjustCommand.getRemark());
        fortuneBill.setConfirm(Boolean.TRUE);
        fortuneBill.setInclude(Boolean.TRUE);
        return fortuneBill;
    }
}
