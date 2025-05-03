package com.fortuneboot.service.fortune;

import cn.hutool.core.lang.Pair;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.fortune.BalanceOperationEnum;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.enums.fortune.CategoryTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.bo.fortune.ApplicationScopeBo;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.command.fortune.FortuneCategoryRelationAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneTagRelationAddCommand;
import com.fortuneboot.domain.entity.fortune.*;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.bill.BillCategoryAmountVo;
import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.factory.fortune.*;
import com.fortuneboot.factory.fortune.model.*;
import com.fortuneboot.repository.fortune.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private final FortuneCategoryRepository fortuneCategoryRepository;

    private final FortuneCategoryRelationService fortuneCategoryRelationService;

    private final FortunePayeeRepository fortunePayeeRepository;

    private final FortuneBookRepository fortuneBookRepository;

    private final FortuneAccountRepository fortuneAccountRepository;

    private final FortuneCategoryRelationRepository fortuneCategoryRelationRepository;

    private final FortuneTagRelationRepository fortuneTagRelationRepository;

    private final FortuneTagRepository fortuneTagRepository;

    private final FortuneFileService fortuneFileService;

    private final FortuneFileRepository fortuneFileRepository;

    private final FortuneTagFactory fortuneTagFactory;
    private final FortuneCategoryFactory fortuneCategoryFactory;

    public PageDTO<FortuneBillBo> getPage(FortuneBillQuery query) {
        IPage<FortuneBillEntity> page = fortuneBillRepository.getPage(query.toPage(), query.addQueryCondition());
        List<FortuneBillBo> list = page.getRecords().stream().map(FortuneBillBo::new).toList();
        if (CollectionUtils.isEmpty(list)) {
            return new PageDTO<>(Collections.emptyList());
        }
        this.fillAccount(list);
        this.fillBook(list);
        this.fillCategory(list);
        this.fillTag(list);
        this.fillPayee(list);
        return new PageDTO<>(list, page.getTotal());
    }

    private void fillCategory(List<FortuneBillBo> list) {
        List<Long> billIdList = list.stream().map(FortuneBillBo::getBillId).filter(Objects::nonNull).toList();
        Map<Long, List<FortuneCategoryRelationEntity>> map = fortuneCategoryRelationRepository.getByBillIdList(billIdList);
        List<Long> categoryIdList = map.values().stream().flatMap(List::stream).map(FortuneCategoryRelationEntity::getCategoryId).distinct().toList();
        List<FortuneCategoryEntity> categoryList = fortuneCategoryRepository.getByIds(categoryIdList);
        Map<Long, FortuneCategoryEntity> categoryMap = categoryList.stream().collect(Collectors.toMap(FortuneCategoryEntity::getCategoryId, Function.identity()));
        for (FortuneBillBo billBo : list) {
            List<FortuneCategoryRelationEntity> relationList = map.get(billBo.getBillId());
            if (CollectionUtils.isEmpty(relationList)) {
                continue;
            }
            billBo.setCategoryAmountPair(new ArrayList<>(relationList.size()));
            for (FortuneCategoryRelationEntity relation : relationList) {
                FortuneCategoryEntity fortuneCategoryEntity = categoryMap.get(relation.getCategoryId());
                BillCategoryAmountVo vo = new BillCategoryAmountVo();
                vo.setCategoryId(fortuneCategoryEntity.getCategoryId());
                vo.setCategoryName(fortuneCategoryEntity.getCategoryName());
                vo.setAmount(relation.getAmount());
                billBo.getCategoryAmountPair().add(vo);
            }
        }
    }

    private void fillTag(List<FortuneBillBo> list) {
        List<Long> billIdList = list.stream().map(FortuneBillBo::getBillId).filter(Objects::nonNull).toList();
        Map<Long, List<FortuneTagRelationEntity>> map = fortuneTagRelationRepository.getByBillIdList(billIdList);
        if (MapUtils.isEmpty(map)) {
            return;
        }
        List<Long> tagIdList = map.values().stream().flatMap(List::stream).map(FortuneTagRelationEntity::getTagId).distinct().toList();
        List<FortuneTagEntity> categoryList = fortuneTagRepository.getByIds(tagIdList);
        Map<Long, FortuneTagEntity> categoryMap = categoryList.stream().collect(Collectors.toMap(FortuneTagEntity::getTagId, Function.identity()));
        for (FortuneBillBo billBo : list) {
            List<FortuneTagRelationEntity> relationList = map.get(billBo.getBillId());
            if (CollectionUtils.isEmpty(relationList)) {
                continue;
            }
            billBo.setTagList(new ArrayList<>(relationList.size()));
            for (FortuneTagRelationEntity relation : relationList) {
                billBo.getTagList().add(categoryMap.get(relation.getTagId()));
            }
        }
    }

    private void fillPayee(List<FortuneBillBo> list) {
        List<Long> payeeIdList = list.stream().map(FortuneBillBo::getPayeeId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(payeeIdList)) {
            return;
        }
        List<FortunePayeeEntity> payeeList = fortunePayeeRepository.getByIdList(payeeIdList);
        Map<Long, String> map = payeeList.stream().collect(Collectors.toMap(FortunePayeeEntity::getPayeeId, FortunePayeeEntity::getPayeeName));
        for (FortuneBillBo billBo : list) {
            billBo.setPayeeName(map.get(billBo.getPayeeId()));
        }
    }

    private void fillBook(List<FortuneBillBo> list) {
        List<Long> bookIdList = list.stream().map(FortuneBillBo::getBookId).toList();
        List<FortuneBookEntity> bookList = fortuneBookRepository.listByIds(bookIdList);
        Map<Long, FortuneBookEntity> map = bookList.stream().collect(Collectors.toMap(FortuneBookEntity::getBookId, Function.identity()));
        for (FortuneBillBo billBo : list) {
            FortuneBookEntity fortuneBookEntity = map.get(billBo.getBookId());
            billBo.setBookName(fortuneBookEntity.getBookName());
            billBo.setCurrencyCode(fortuneBookEntity.getDefaultCurrency());
        }
    }

    private void fillAccount(List<FortuneBillBo> list) {
        List<Long> accountIdList = new ArrayList<>(list.stream().map(FortuneBillBo::getAccountId).toList());
        List<Long> toAccountIdList = list.stream().map(FortuneBillBo::getToAccountId).toList();
        accountIdList.addAll(toAccountIdList);
        List<FortuneAccountEntity> accountList = fortuneAccountRepository.getByIds(accountIdList);
        Map<Long, String> map = accountList.stream().collect(Collectors.toMap(FortuneAccountEntity::getAccountId, FortuneAccountEntity::getAccountName));
        for (FortuneBillBo billBo : list) {
            billBo.setAccountName(map.get(billBo.getAccountId()));
            billBo.setToAccountName(map.get(billBo.getToAccountId()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(FortuneBillAddCommand addCommand) {
        // 主模型操作
        FortuneBillModel fortuneBillModel = fortuneBillFactory.create();
        fortuneBillModel.loadAddCommand(addCommand);
        fortuneBillModel.checkBookId(fortuneBillModel.getBookId());
        // 收款人校验提前
        if (Objects.nonNull(addCommand.getPayeeId())) {
            FortunePayeeModel payee = fortunePayeeFactory.loadById(addCommand.getPayeeId());
            fortuneBillModel.checkPayeeExist(payee);
            fortuneBillModel.checkPayeeEnable(payee);
        }
        // 使用枚举类型直接比较
        BillTypeEnum billType = BillTypeEnum.getByValue(addCommand.getBillType());
        if (billType == BillTypeEnum.EXPENSE || billType == BillTypeEnum.INCOME) {
            // 金额计算优化
            BigDecimal amount = addCommand.getCategoryAmountPair().stream()
                    .map(Pair::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            fortuneBillModel.setAmount(amount);
            fortuneBillModel.setConvertedAmount(amount);
        }
        // 资金操作
        this.confirmBalance(fortuneBillModel);
        // 持久化主记录
        fortuneBillModel.insert();
        // 批量标签处理
        this.processTagRelations(addCommand.getTagIdList(), fortuneBillModel);
        // 批量分类处理
        this.processCategoryRelations(addCommand.getCategoryAmountPair(), fortuneBillModel);
        fortuneFileService.batchAdd(fortuneBillModel.getBillId(), addCommand.getFileList());
    }

    /**
     * 批量处理标签关联
     */
    private void processTagRelations(List<Long> tagIds, FortuneBillModel fortuneBillModel) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        List<FortuneTagModel> fortuneTagModels = fortuneTagFactory.loadByIds(tagIds);
        // 校验标签启用
        fortuneBillModel.checkTagListEnable(fortuneTagModels);
        // 构建新增command
        List<FortuneTagRelationAddCommand> commands = tagIds.stream()
                .map(tagId -> new FortuneTagRelationAddCommand(fortuneBillModel.getBillId(), tagId))
                .collect(Collectors.toList());
        // 需要实现批量插入方法
        fortuneTagRelationService.batchAdd(commands);
    }

    /**
     * 批量处理分类关联
     */
    private void processCategoryRelations(List<Pair<Long, BigDecimal>> categories, FortuneBillModel fortuneBillModel) {
        if (CollectionUtils.isEmpty(categories)) {
            return;
        }
        List<Long> categoryIds = categories.stream().map(Pair::getKey).toList();
        // 加载分类
        List<FortuneCategoryModel> fortuneCategoryModels = fortuneCategoryFactory.loadByIds(categoryIds);
        // 校验分类启用
        fortuneBillModel.checkCategoryListEnable(fortuneCategoryModels);
        // 构建分类-账单关系
        List<FortuneCategoryRelationAddCommand> commands = categories.stream()
                .map(pair -> new FortuneCategoryRelationAddCommand(fortuneBillModel.getBillId(), pair.getKey(), pair.getValue()))
                .collect(Collectors.toList());
        // 需要实现批量插入方法
        fortuneCategoryRelationService.batchAdd(commands);
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
        fortuneTagRelationRepository.removeByBillId(billId);
        // 删除分类
        fortuneCategoryRelationRepository.removeByBillId(billId);
        // 删除账单
        fortuneBillModel.deleteById();
        // 删除账单附件
        fortuneFileRepository.removeByBillId(billId);
    }

    /**
     * 确认余额
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmBalance(FortuneBillModel fortuneBillModel) {
        if (!fortuneBillModel.getConfirm() || Objects.isNull(fortuneBillModel.getAccountId())) {
            return;
        }
        // 加载源账户并获取账单类型
        FortuneAccountModel sourceAccount = fortuneAccountFactory.loadById(fortuneBillModel.getAccountId());
        fortuneBillModel.checkAccountEnable(sourceAccount);
        BillTypeEnum billType = BillTypeEnum.getByValue(fortuneBillModel.getBillType());
        // 根据账单类型处理账户余额
        switch (billType) {
            case INCOME:
                // 校验可收入
                sourceAccount.checkCanIncome();
                this.adjustBalance(sourceAccount, fortuneBillModel.getAmount(), BalanceOperationEnum.ADD);
                break;
            case PROFIT, ADJUST:
                this.adjustBalance(sourceAccount, fortuneBillModel.getAmount(), BalanceOperationEnum.ADD);
                break;
            case EXPENSE:
                // 校验可支出
                sourceAccount.checkCanExpense();
                this.adjustBalance(sourceAccount, fortuneBillModel.getAmount(), BalanceOperationEnum.SUBTRACT);
                break;
            case LOSS:
                this.adjustBalance(sourceAccount, fortuneBillModel.getAmount(), BalanceOperationEnum.SUBTRACT);
                break;
            case TRANSFER:
                // 校验可转出
                sourceAccount.checkCanTransferOut();
                this.handleTransferOperation(sourceAccount, fortuneBillModel);
                break;
            case null, default:
                log.warn("Unsupported bill type: {}", billType);
                throw new ApiException(ErrorCode.Business.BILL_TYPE_ILLEGAL, fortuneBillModel.getBillType());
        }

        // 更新源账户信息
        sourceAccount.updateById();
    }

    /**
     * 辅助方法：调整账户余额
     */
    private void adjustBalance(FortuneAccountModel account, BigDecimal amount, BalanceOperationEnum operation) {
        if (account == null || amount == null) return;

        BigDecimal newBalance = operation == BalanceOperationEnum.ADD ?
                account.getBalance().add(amount) :
                account.getBalance().subtract(amount);
        account.setBalance(newBalance);
    }

    /**
     * 辅助方法：处理转账操作
     */
    private void handleTransferOperation(FortuneAccountModel sourceAccount, FortuneBillModel bill) {
        // 验证目标账户是否存在
        if (Objects.isNull(bill.getToAccountId())) {
            log.error("Missing target account for transfer operation");
            return;
        }
        // 调整源账户余额
        this.adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.SUBTRACT);
        // 加载目标账户并转换金额
        FortuneAccountModel targetAccount = fortuneAccountFactory.loadById(bill.getToAccountId());
        // 校验账户启用状态
        bill.checkAccountEnable(targetAccount);
        // 校验可转入
        targetAccount.checkCanTransferIn();
        BigDecimal convertedAmount = convertCurrency(
                bill.getAmount(),
                sourceAccount.getCurrencyCode(),
                targetAccount.getCurrencyCode(),
                applicationScopeBo.getCurrencyTemplateBoList()
        );
        // 调整目标账户余额
        this.adjustBalance(targetAccount, convertedAmount, BalanceOperationEnum.ADD);
        // 更新目标账户并记录转换金额
        targetAccount.updateById();
        bill.setConvertedAmount(convertedAmount);
    }

    /**
     * 辅助方法：货币转换
     */
    public BigDecimal convertCurrency(BigDecimal amount, String sourceCurrency, String targetCurrency, List<CurrencyTemplateBo> aprList) {
        if (sourceCurrency.equals(targetCurrency)) {
            return amount;
        }
        // 获取 sourceCurrency 对美元的汇率
        BigDecimal aprSourceToRMB = aprList.stream()
                .filter(apr -> apr.getCurrencyName().equals(sourceCurrency))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.APR_NOT_FOUND, sourceCurrency, " -> 美元"));
        // 获取 targetCurrency 对美元的汇率
        BigDecimal aprTargetToRMB = aprList.stream()
                .filter(apr -> apr.getCurrencyName().equals(targetCurrency))
                .findFirst()
                .map(CurrencyTemplateBo::getRate)
                .orElseThrow(() -> new ApiException(ErrorCode.Business.APR_NOT_FOUND, "美元", targetCurrency));
        // 计算目标货币金额
        return amount.multiply(aprSourceToRMB)
                .divide(aprTargetToRMB, 10, RoundingMode.HALF_UP);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(FortuneBillModel bill) {
        // 验证退款基本条件
        if (!isValidRefundRequest(bill)) {
            log.warn("Invalid refund request for bill: {}", bill.getBillId());
            return;
        }
        // 加载账户并处理退款
        FortuneAccountModel sourceAccount = fortuneAccountFactory.loadById(bill.getAccountId());
        BillTypeEnum billType = BillTypeEnum.getByValue(bill.getBillType());
        processRefundByType(sourceAccount, bill, billType);
    }

    /**
     * 条件验证方法
     */
    private boolean isValidRefundRequest(FortuneBillModel bill) {
        return bill.getConfirm() && Objects.nonNull(bill.getAccountId())
                && Objects.nonNull(bill.getAmount()) && bill.getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 退款处理核心逻辑
     */
    private void processRefundByType(FortuneAccountModel sourceAccount, FortuneBillModel bill, BillTypeEnum billType) {
        switch (billType) {
            case INCOME, PROFIT, ADJUST:
                this.adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.SUBTRACT);
                break;
            case EXPENSE, LOSS:
                this.adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.ADD);
                break;
            case TRANSFER:
                this.handleTransferRefund(sourceAccount, bill);
                break;
            default:
                log.warn("Unsupported refund type: {}", billType);
                break;
        }
        sourceAccount.updateById();
    }

    /**
     * 处理转账退款
     */
    private void handleTransferRefund(FortuneAccountModel sourceAccount, FortuneBillModel bill) {
        // 验证转账必要参数
        if (Objects.isNull(bill.getToAccountId()) || Objects.isNull(bill.getConvertedAmount())) {
            throw new ApiException(ErrorCode.Business.BILL_TRANSFER_PARAMETER_ERROR);
        }
        // 逆向处理源账户
        this.adjustBalance(sourceAccount, bill.getAmount(), BalanceOperationEnum.ADD);
        // 处理目标账户
        FortuneAccountModel targetAccount = fortuneAccountFactory.loadById(bill.getToAccountId());
        this.adjustBalance(targetAccount, bill.getConvertedAmount(), BalanceOperationEnum.SUBTRACT);
        targetAccount.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByBookId(Long bookId) {
        List<FortuneBillEntity> billList = fortuneBillRepository.getByBookId(bookId);
        if (CollectionUtils.isEmpty(billList)) {
            return;
        }
        List<Long> billIds = billList.stream().map(FortuneBillEntity::getBillId).toList();
        fortuneBillRepository.removeBatchByIds(billIds);
        fortuneCategoryRelationRepository.removeByBillIds(billIds);
        fortuneTagRelationRepository.removeByBillIds(billIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByBookIds(List<Long> bookIds) {
        List<FortuneBillEntity> billList = fortuneBillRepository.getByBookIds(bookIds);
        List<Long> billIds = billList.stream().map(FortuneBillEntity::getBillId).toList();
        fortuneBillRepository.removeBatchByIds(billIds);
        fortuneCategoryRelationRepository.removeByBillIds(billIds);
        fortuneTagRelationRepository.removeByBillIds(billIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        fortuneBillModel.setConfirm(Boolean.TRUE);
        this.confirmBalance(fortuneBillModel);
        fortuneBillModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void unConfirm(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        this.refundBalance(fortuneBillModel);
        fortuneBillModel.setConfirm(Boolean.FALSE);
        fortuneBillModel.updateById();
    }

    public void include(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        fortuneBillModel.setInclude(Boolean.TRUE);
        fortuneBillModel.updateById();
    }

    public void exclude(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        fortuneBillModel.setInclude(Boolean.FALSE);
        fortuneBillModel.updateById();
    }

    public BillStatisticsVo getBillStatistics(FortuneBillQuery query) {
        query.setInclude(Boolean.TRUE);
        return fortuneBillRepository.getBillStatistics(query);
    }

    public List<FortuneLineVo> getExpenseTrends(BillTrendsQuery billTrendsQuery) {
        List<FortuneLineVo> originData = fortuneBillRepository.getExpenseTrends(billTrendsQuery);
        return this.completeTimeSeries(originData, billTrendsQuery.getTimeGranularity(), billTrendsQuery.getTimePoint());
    }

    public List<FortuneLineVo> getIncomeTrends(BillTrendsQuery billTrendsQuery) {
        List<FortuneLineVo> originData = fortuneBillRepository.getIncomeTrends(billTrendsQuery);
        return this.completeTimeSeries(originData, billTrendsQuery.getTimeGranularity(), billTrendsQuery.getTimePoint());
    }

    private final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    private List<FortuneLineVo> completeTimeSeries(List<FortuneLineVo> originData, int timeGranularity, LocalDateTime baseTime) {
        // 参数校验
        if (originData == null) originData = new ArrayList<>();
        if (baseTime == null) baseTime = LocalDateTime.now();

        // 根据粒度类型生成对应时间序列
        List<String> timeKeys = generateTimeKeys(timeGranularity, baseTime, originData);

        // 构建数据映射表
        Map<String, FortuneLineVo> dataMap = originData.stream().filter(vo -> vo.getName() != null).collect(Collectors.toMap(FortuneLineVo::getName, Function.identity()));

        // 补全缺失数据
        return timeKeys.stream().map(key -> dataMap.containsKey(key) ? dataMap.get(key) : new FortuneLineVo(key, BigDecimal.ZERO)).toList();
    }

    private List<String> generateTimeKeys(int granularity, LocalDateTime baseTime, List<FortuneLineVo> data) {
        LocalDate baseDate = baseTime.toLocalDate();

        switch (granularity) {
            case 1: // 过去7天（包含当天）
                return IntStream.rangeClosed(0, 6).mapToObj(offset -> baseDate.minusDays(6 - offset)).map(DAY_FORMATTER::format).toList();
            case 2: // 过去30天
                return IntStream.rangeClosed(0, 29).mapToObj(offset -> baseDate.minusDays(29 - offset)).map(DAY_FORMATTER::format).toList();
            case 3: { // 过去12个月（自然月）
                YearMonth currentMonth = YearMonth.from(baseTime);
                return IntStream.rangeClosed(0, 11).mapToObj(i -> currentMonth.minusMonths(11 - i)).map(month -> month.format(MONTH_FORMATTER)).toList();
            }
            case 4: { // 跨年补全
                int currentYear = Year.now().getValue();
                int minYear = data.stream()
                        .filter(vo -> vo.getName() != null)
                        .mapToInt(vo -> {
                            try {
                                return Year.parse(vo.getName(), YEAR_FORMATTER).getValue();
                            } catch (Exception e) {
                                // 无效数据按当前年处理
                                return currentYear;
                            }
                        })
                        .min()
                        // 无数据时使用当前年
                        .orElse(currentYear);
                return IntStream.rangeClosed(minYear, currentYear).mapToObj(year -> Year.of(year).format(YEAR_FORMATTER)).toList();
            }

            default:
                throw new IllegalArgumentException("Invalid time granularity: " + granularity);
        }
    }

    public List<FortunePieVo> getCategoryExpense(CategoryIncludeQuery query) {
        return fortuneBillRepository.getCategoryInclude(CategoryTypeEnum.EXPENSE, query);
    }

    public List<FortunePieVo> getCategoryIncome(CategoryIncludeQuery query) {
        return fortuneBillRepository.getCategoryInclude(CategoryTypeEnum.INCOME, query);
    }

    public List<FortuneBarVo> getTagExpense(TagIncludeQuery query) {
        return fortuneBillRepository.getTagInclude(CategoryTypeEnum.EXPENSE, query);
    }

    public List<FortuneBarVo> getTagIncome(TagIncludeQuery query) {
        return fortuneBillRepository.getTagInclude(CategoryTypeEnum.INCOME, query);
    }

    public List<FortunePieVo> getPayeeExpense(PayeeIncludeQuery query) {
        return fortuneBillRepository.getPayeeInclude(CategoryTypeEnum.EXPENSE, query);
    }

    public List<FortunePieVo> getPayeeIncome(PayeeIncludeQuery query) {
        return fortuneBillRepository.getPayeeInclude(CategoryTypeEnum.INCOME, query);
    }
}
