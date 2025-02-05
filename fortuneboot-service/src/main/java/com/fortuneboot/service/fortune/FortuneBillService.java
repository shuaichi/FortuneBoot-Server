package com.fortuneboot.service.fortune;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.CurrencyTemplateBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.factory.fortune.*;
import com.fortuneboot.factory.fortune.model.*;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 账单service
 *
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

    private final ApplicationScopeBo applicationScopeBo;

    private final FortuneTagRelationService fortuneTagRelationService;

    private final FortunePayeeFactory fortunePayeeFactory;

    private final FortuneCategoryRelationService fortuneCategoryRelationService;

    public IPage<FortuneBillEntity> getPage(FortuneBillQuery query) {
        return fortuneBillRepository.page(query.toPage(), query.addQueryCondition());
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneBillAddCommand addCommand) {
        // 参数校验前置
        Objects.requireNonNull(addCommand, "addCommand不能为空");
        if (CollectionUtils.isEmpty(addCommand.getCategoryList())) {
            throw new IllegalArgumentException("categoryList不能为空");
        }

        // 主模型操作
        FortuneBillModel fortuneBillModel = fortuneBillFactory.create();
        fortuneBillModel.loadAddCommand(addCommand);
        fortuneBillModel.checkBookId(fortuneBillModel.getBookId());

        // 收款人校验提前
        if (Objects.nonNull(addCommand.getPayeeId())) {
            FortunePayeeModel payee = fortunePayeeFactory.loadById(addCommand.getPayeeId());
            fortuneBillModel.checkPayeeExist(payee);
        }

        // 金额计算优化
        BigDecimal amount = addCommand.getCategoryList().stream()
                .map(Pair::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 使用枚举类型直接比较
        BillTypeEnum billType = BillTypeEnum.getByValue(addCommand.getBillType());
        if (billType == BillTypeEnum.EXPENSE || billType == BillTypeEnum.INCOME) {
            fortuneBillModel.setAmount(amount);
            fortuneBillModel.setConvertedAmount(amount);
        }

        // 资金操作
        this.confirmBalance(fortuneBillModel);

        // 持久化主记录
        fortuneBillModel.insert();
        Long billId = fortuneBillModel.getBillId();  // 提前获取ID

        // 批量标签处理
        processTagRelations(addCommand.getTagIdList(), billId);

        // 批量分类处理
        processCategoryRelations(addCommand.getCategoryList(), billId);
    }

    /**
     * 批量处理标签关联
     */
    private void processTagRelations(List<Long> tagIds, Long billId) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<FortuneTagRelationAddCommand> commands = tagIds.stream()
                .map(tagId -> new FortuneTagRelationAddCommand(billId, tagId))
                .collect(Collectors.toList());
        fortuneTagRelationService.batchAdd(commands);  // 需要实现批量插入方法
    }

    /**
     * 批量处理分类关联
     */
    private void processCategoryRelations(List<Pair<Long, BigDecimal>> categories, Long billId) {
        List<FortuneCategoryRelationAddCommand> commands = categories.stream()
                .map(pair -> new FortuneCategoryRelationAddCommand(billId, pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
        fortuneCategoryRelationService.batchAdd(commands);  // 需要实现批量插入方法
    }

    // 修改的逻辑，删除旧的，新增一条新纪录
    @Transactional(rollbackFor = Exception.class)
    public void modify(FortuneBillModifyCommand modifyCommand) {
        this.remove(modifyCommand.getBookId(), modifyCommand.getBillId());
        this.add(modifyCommand);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        // 账户金额回滚
        this.refundBalance(fortuneBillModel);
        // 删除标签
        fortuneTagRelationService.removeByBillId(billId);
        // 删除分类
        fortuneCategoryRelationService.removeByBillId(billId);
        fortuneBillModel.deleteById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmBalance(FortuneBillModel fortuneBillModel) {
        if (fortuneBillModel.getConfirm() && Objects.nonNull(fortuneBillModel.getAccountId())) {
            FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(fortuneBillModel.getAccountId());
            BillTypeEnum billType = BillTypeEnum.getByValue(fortuneBillModel.getBillType());
            switch (billType) {
                case INCOME:
                case PROFIT:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().add(fortuneBillModel.getAmount()));
                    break;
                case EXPENSE:
                case LOSS:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(fortuneBillModel.getAmount()));
                    break;
                case TRANSFER:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().add(fortuneBillModel.getAmount()));
                    FortuneAccountModel fortuneAccountToModel = fortuneAccountFactory.loadById(fortuneBillModel.getToAccountId());
                    fortuneAccountToModel.setBalance(fortuneAccountModel.getBalance().subtract(fortuneBillModel.getAmount()));
                    // 汇率转换
                    if (!StringUtils.equals(fortuneAccountToModel.getCurrencyCode(), fortuneAccountModel.getCurrencyCode())) {
                        List<CurrencyTemplateBo> currencyTemplateBoList = applicationScopeBo.getCurrencyTemplateBoList();
                        Optional<CurrencyTemplateBo> currency = currencyTemplateBoList.stream().filter(item -> StringUtils.equals(item.getCurrencyName(), fortuneAccountModel.getCurrencyCode())).findFirst();
                        if (currency.isPresent()) {
                            CurrencyTemplateBo currencyTemplateBo = currency.get();
                            BigDecimal convertedAmount = fortuneAccountModel.getBalance().multiply(currencyTemplateBo.getRate());
                            fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(convertedAmount));
                            fortuneBillModel.setConvertedAmount(convertedAmount);
                        }
                    }
                    fortuneAccountToModel.updateById();
                case ADJUST:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().add(fortuneBillModel.getAmount()));
                case null, default:
                    break;
            }
            fortuneAccountModel.updateById();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(FortuneBillModel fortuneBillModel) {
        if (fortuneBillModel.getConfirm() && Objects.nonNull(fortuneBillModel.getAccountId())) {
            FortuneAccountModel fortuneAccountModel = fortuneAccountFactory.loadById(fortuneBillModel.getAccountId());
            BillTypeEnum billType = BillTypeEnum.getByValue(fortuneBillModel.getBillType());
            switch (billType) {
                case INCOME:
                case PROFIT:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(fortuneBillModel.getAmount()));
                    break;
                case EXPENSE:
                case LOSS:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().add(fortuneBillModel.getAmount()));
                    break;
                case TRANSFER:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(fortuneBillModel.getAmount()));
                    FortuneAccountModel fortuneAccountToModel = fortuneAccountFactory.loadById(fortuneBillModel.getToAccountId());
                    fortuneAccountToModel.setBalance(fortuneAccountModel.getBalance().add(fortuneBillModel.getConvertedAmount()));
                    fortuneAccountToModel.updateById();
                case ADJUST:
                    fortuneAccountModel.setBalance(fortuneAccountModel.getBalance().subtract(fortuneBillModel.getAmount()));
                case null, default:
                    break;
            }
            fortuneAccountModel.updateById();
        }
    }
}
