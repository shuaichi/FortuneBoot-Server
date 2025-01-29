package com.fortuneboot.service.fortune;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.CurrencyTemplateBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.factory.fortune.*;
import com.fortuneboot.factory.fortune.model.*;
import com.fortuneboot.repository.fortune.FortuneAccountRepository;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import com.fortuneboot.repository.fortune.FortuneTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:42
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneBillService {

    private final FortuneBillRepository fortuneBillRepository;

    private final FortuneBillFactory fortuneBillFactory;

    private final FortuneAccountFactory fortuneAccountFactory;

    private final FortuneBookFactory fortuneBookFactory;

    private final ApplicationScopeBo applicationScopeBo;

    private final FortuneTagRelationService fortuneTagRelationService;

    private final FortunePayeeFactory fortunePayeeFactory;

    public IPage<FortuneBillEntity> getPage(FortuneBillQuery query) {
        return fortuneBillRepository.page(query.toPage(), query.addQueryCondition());
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneBillAddCommand addCommand) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.create();
        fortuneBillModel.loadAddCommand(addCommand);
        fortuneBillModel.checkBookId(fortuneBillModel.getBookId());
        BigDecimal amount = addCommand.getCategoryList().stream().map(Pair::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (Objects.nonNull(addCommand.getAccountId())) {
            // 修改账户金额
            FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(addCommand.getAccountId());
            fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(amount));
            fortuneAccountModel.updateById();
        }
        if (Objects.equals(addCommand.getBillType(), BillTypeEnum.EXPENSE.getValue()) || Objects.equals(addCommand.getBillType(), BillTypeEnum.INCOME.getValue())) {
            fortuneBillModel.setAmount(amount);
            //转换汇率
            FortuneBookModel fortuneBookModel = fortuneBookFactory.loadById(addCommand.getBookId());
            fortuneBillModel.setConvertedAmount(amount);
            if (!StringUtils.equals(addCommand.getCurrencyCode(), fortuneBookModel.getDefaultCurrency())) {
                List<CurrencyTemplateBo> currencyTemplateBoList = applicationScopeBo.getCurrencyTemplateBoList();
                Optional<CurrencyTemplateBo> currency = currencyTemplateBoList.stream().filter(item -> StringUtils.equals(item.getCurrencyName(), fortuneBookModel.getDefaultCurrency())).findFirst();
                currency.ifPresent(currencyTemplateBo -> fortuneBillModel.setConvertedAmount(amount.divide(currencyTemplateBo.getRate(), 2, RoundingMode.HALF_UP)));
            }
            fortuneBillModel.setConvertedAmount(amount);
        }
        if (Objects.equals(addCommand.getBillType(), BillTypeEnum.TRANSFER.getValue())) {
            FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(addCommand.getAccountToId());
            fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().add(amount));
            if (!StringUtils.equals(addCommand.getCurrencyCode(), fortuneAccountModel.getCurrencyCode())) {
                List<CurrencyTemplateBo> currencyTemplateBoList = applicationScopeBo.getCurrencyTemplateBoList();
                Optional<CurrencyTemplateBo> currency = currencyTemplateBoList.stream().filter(item -> StringUtils.equals(item.getCurrencyName(), fortuneAccountModel.getCurrencyCode())).findFirst();
                currency.ifPresent(currencyTemplateBo -> fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().add(currencyTemplateBo.getRate())));
            }
            fortuneBillModel.updateById();
        }
        if (Objects.nonNull(addCommand.getPayeeId())) {
            FortunePayeeModel fortunePayeeModel = fortunePayeeFactory.loadById(addCommand.getPayeeId());
            fortuneBillModel.checkPayeeExist(fortunePayeeModel);
        }
        fortuneBillModel.insert();
        if (CollectionUtils.isNotEmpty(addCommand.getTagIdList())) {
            for (Long tagId : addCommand.getTagIdList()) {
                Long billId = fortuneBillModel.getBillId();
                FortuneTagRelationAddCommand fortuneTagRelationAddCommand = new FortuneTagRelationAddCommand();
                fortuneTagRelationAddCommand.setBillId(billId);
                fortuneTagRelationAddCommand.setTagId(tagId);
                fortuneTagRelationService.add(fortuneTagRelationAddCommand);
            }
        }
        for (Pair<Long,BigDecimal> category:addCommand.getCategoryList()){
            Long billId = fortuneBillModel.getBillId();

        }
    }

    public void modify(FortuneBillModifyCommand modifyCommand) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(modifyCommand.getBillId());
        fortuneBillModel.loadModifyCommand(modifyCommand);
        fortuneBillModel.checkBookId(modifyCommand.getBookId());
        fortuneBillModel.updateById();
    }

    public void remove(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        fortuneBillModel.deleteById();
    }
}
