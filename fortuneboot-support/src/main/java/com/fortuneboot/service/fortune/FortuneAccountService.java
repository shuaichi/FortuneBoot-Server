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
import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 22:48
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneAccountService {

    private final FortuneAccountRepo fortuneAccountRepo;

    private final FortuneAccountFactory fortuneAccountFactory;

    private final FortuneBillRepo fortuneBillRepo;

    private final FortuneBillService fortuneBillService;

    private final FortuneGroupFactory fortuneGroupFactory;

    private final ApplicationScopeBo applicationScopeBo;

    public IPage<FortuneAccountEntity> getPage(FortuneAccountQuery query) {
        return fortuneAccountRepo.page(query.toPage(), query.addQueryCondition());
    }

    public List<FortuneAccountEntity> getEnableAccountList(Long groupId) {
        return fortuneAccountRepo.getEnableAccountList(groupId);
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
        Boolean exist = fortuneBillRepo.existByAccount(accountId);
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
        // 1) 获取分组默认币种
        String defaultCurrency = fortuneGroupFactory.loadById(groupId).getDefaultCurrency();

        // 2) 查询该分组启用账户
        List<FortuneAccountEntity> accounts = fortuneAccountRepo.getEnableAccountList(groupId);

        // 3) 获取汇率模板（以 USD 为基础：1 USD = rate 本币）
        List<CurrencyTemplateBo> rateList = applicationScopeBo.getCurrencyTemplateBoList();

        // 4) 将各账户余额按“账户币种 → USD → 分组默认币种”转换，并累计
        List<FortunePieVo> result = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (FortuneAccountEntity acc : accounts) {
            if (Boolean.TRUE.equals(acc.getInclude())
                    && acc.getBalance() != null
                    && acc.getBalance().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal converted = convertCurrency(
                        acc.getBalance(),
                        acc.getCurrencyCode(),
                        defaultCurrency,
                        rateList
                ).setScale(2, RoundingMode.HALF_UP);

                if (converted.compareTo(BigDecimal.ZERO) > 0) {
                    FortunePieVo vo = new FortunePieVo();
                    vo.setName(acc.getAccountName());
                    vo.setValue(converted);
                    result.add(vo);
                    total = total.add(converted);
                }
            }
        }

        // 5) 计算百分比
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            for (FortunePieVo vo : result) {
                BigDecimal percent = vo.getValue()
                        .divide(total, 10, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                vo.setPercent(percent);
            }
        } else {
            for (FortunePieVo vo : result) {
                vo.setPercent(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            }
        }

        return result;
    }

    /**
     * 货币转换（账本汇率以 USD 为基础：1 USD = rate 本币）
     * 公式：
     * 源币种 → USD: amount ÷ sourceRate
     * USD → 目标币种: usdAmount × targetRate
     */
    private BigDecimal convertCurrency(BigDecimal amount,
                                       String sourceCurrency,
                                       String targetCurrency,
                                       List<CurrencyTemplateBo> rateList) {
        if (sourceCurrency == null || targetCurrency == null || sourceCurrency.equals(targetCurrency)) {
            return amount;
        }

        BigDecimal sourceRate = rateList.stream()
                .filter(r -> sourceCurrency.equals(r.getCurrencyName()))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.APR_NOT_FOUND, sourceCurrency, "USD"));

        BigDecimal targetRate = rateList.stream()
                .filter(r -> targetCurrency.equals(r.getCurrencyName()))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.APR_NOT_FOUND, "USD", targetCurrency));

        if (sourceRate == null || sourceRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.Business.INVALID_EXCHANGE_RATE, sourceCurrency, sourceRate);
        }
        if (targetRate == null || targetRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(ErrorCode.Business.INVALID_EXCHANGE_RATE, targetCurrency, targetRate);
        }

        BigDecimal usdAmount = amount.divide(sourceRate, 10, RoundingMode.HALF_UP);
        return usdAmount.multiply(targetRate);
    }


    public List<FortunePieVo> getTotalLiabilities(Long groupId) {
        return fortuneAccountRepo.getTotalLiabilities(groupId);
    }

    public FortuneAssetsLiabilitiesVo getFortuneAssetsLiabilities(Long groupId) {
        return fortuneAccountRepo.getFortuneAssetsLiabilities(groupId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void balanceAdjust(FortuneAccountAdjustCommand adjustCommand) {
        FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(adjustCommand.getAccountId());
        FortuneBillAddCommand fortuneBillAddCommand = fortuneAccountModel.loadAdjustCommand(adjustCommand);
        fortuneBillService.add(fortuneBillAddCommand);
        fortuneAccountModel.setBalance(adjustCommand.getBalance());
        fortuneAccountModel.updateById();
    }

}
