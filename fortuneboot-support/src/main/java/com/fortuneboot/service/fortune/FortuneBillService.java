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
import com.fortuneboot.factory.fortune.factory.*;
import com.fortuneboot.factory.fortune.model.*;
import com.fortuneboot.repository.fortune.*;
import com.fortuneboot.strategy.bill.BillProcessStrategy;
import com.fortuneboot.strategy.bill.BillStrategyContext;
import com.fortuneboot.strategy.bill.BillStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final FortuneBillRepo fortuneBillRepo;

    private final FortuneBillFactory fortuneBillFactory;

    private final FortuneAccountFactory fortuneAccountFactory;

    private final FortuneTagRelationService fortuneTagRelationService;

    private final FortunePayeeFactory fortunePayeeFactory;

    private final FortuneCategoryRepo fortuneCategoryRepo;

    private final FortuneCategoryRelationService fortuneCategoryRelationService;

    private final FortunePayeeRepo fortunePayeeRepo;

    private final FortuneBookRepo fortuneBookRepo;

    private final FortuneAccountRepo fortuneAccountRepo;

    private final FortuneCategoryRelationRepo fortuneCategoryRelationRepo;

    private final FortuneTagRelationRepo fortuneTagRelationRepo;

    private final FortuneTagRepo fortuneTagRepo;

    private final FortuneFileService fortuneFileService;

    private final FortuneFileRepo fortuneFileRepo;

    private final FortuneTagFactory fortuneTagFactory;

    private final FortuneCategoryFactory fortuneCategoryFactory;

    private final FortuneBookFactory fortuneBookFactory;

    private final BillStrategyFactory strategyFactory;


    private final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");


    public PageDTO<FortuneBillBo> getPage(FortuneBillQuery query) {
        IPage<FortuneBillEntity> page = fortuneBillRepo.getPage(query.toPage(), query.addQueryCondition());
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
        Map<Long, List<FortuneCategoryRelationEntity>> map = fortuneCategoryRelationRepo.getByBillIdList(billIdList);
        List<Long> categoryIdList = map.values().stream().flatMap(List::stream).map(FortuneCategoryRelationEntity::getCategoryId).distinct().toList();
        List<FortuneCategoryEntity> categoryList = fortuneCategoryRepo.getByIds(categoryIdList);
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
        Map<Long, List<FortuneTagRelationEntity>> map = fortuneTagRelationRepo.getByBillIdList(billIdList);
        if (MapUtils.isEmpty(map)) {
            return;
        }
        List<Long> tagIdList = map.values().stream().flatMap(List::stream).map(FortuneTagRelationEntity::getTagId).distinct().toList();
        List<FortuneTagEntity> categoryList = fortuneTagRepo.getByIds(tagIdList);
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
        List<FortunePayeeEntity> payeeList = fortunePayeeRepo.getByIdList(payeeIdList);
        Map<Long, String> map = payeeList.stream().collect(Collectors.toMap(FortunePayeeEntity::getPayeeId, FortunePayeeEntity::getPayeeName));
        for (FortuneBillBo billBo : list) {
            billBo.setPayeeName(map.get(billBo.getPayeeId()));
        }
    }

    private void fillBook(List<FortuneBillBo> list) {
        List<Long> bookIdList = list.stream().map(FortuneBillBo::getBookId).toList();
        List<FortuneBookEntity> bookList = fortuneBookRepo.listByIds(bookIdList);
        Map<Long, FortuneBookEntity> map = bookList.stream().collect(Collectors.toMap(FortuneBookEntity::getBookId, Function.identity()));
        for (FortuneBillBo billBo : list) {
            FortuneBookEntity fortuneBookEntity = map.get(billBo.getBookId());
            billBo.setBookName(fortuneBookEntity.getBookName());
        }
    }

    private void fillAccount(List<FortuneBillBo> list) {
        List<Long> accountIdList = new ArrayList<>(list.stream().map(FortuneBillBo::getAccountId).toList());
        List<Long> toAccountIdList = list.stream().map(FortuneBillBo::getToAccountId).toList();
        accountIdList.addAll(toAccountIdList);
        List<FortuneAccountEntity> accountList = fortuneAccountRepo.getByIds(accountIdList);
        Map<Long, FortuneAccountEntity> map = accountList.stream().collect(Collectors.toMap(FortuneAccountEntity::getAccountId, Function.identity()));
        for (FortuneBillBo billBo : list) {
            FortuneAccountEntity account = map.get(billBo.getAccountId());
            FortuneAccountEntity toAccount = map.get(billBo.getToAccountId());
            billBo.setAccountName(Optional.ofNullable(account).map(FortuneAccountEntity::getAccountName).orElse(StringUtils.EMPTY));
            billBo.setCurrencyCode(Optional.ofNullable(account).map(FortuneAccountEntity::getCurrencyCode).orElse(StringUtils.EMPTY));
            billBo.setToAccountName(Optional.ofNullable(toAccount).map(FortuneAccountEntity::getAccountName).orElse(StringUtils.EMPTY));
            billBo.setToCurrencyCode(Optional.ofNullable(toAccount).map(FortuneAccountEntity::getCurrencyCode).orElse(StringUtils.EMPTY));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Long add(FortuneBillAddCommand addCommand) {
        // 主模型操作
        FortuneBillModel fortuneBillModel = fortuneBillFactory.create();
        fortuneBillModel.loadAddCommand(addCommand);

        // 收款人校验提前
        if (Objects.nonNull(addCommand.getPayeeId())) {
            FortunePayeeModel payee = fortunePayeeFactory.loadById(addCommand.getPayeeId());
            fortuneBillModel.checkPayeeExist(payee);
            fortuneBillModel.checkPayeeEnable(payee);
        }

        // 构建策略执行上下文
        BillStrategyContext context = this.buildContext(fortuneBillModel);

        // 获取对应策略并执行
        BillProcessStrategy strategy = strategyFactory.getStrategy(fortuneBillModel.getBillType());

        // 汇率转换
        strategy.convertRate(context);

        // 资金操作
        strategy.confirmBalance(context);

        // 持久化主记录
        fortuneBillModel.insert();

        // 批量标签处理
        this.processTagRelations(addCommand.getTagIdList(), fortuneBillModel);

        // 批量分类处理
        this.processCategoryRelations(addCommand.getCategoryAmountPair(), fortuneBillModel);

        fortuneFileService.batchAdd(fortuneBillModel.getBillId(), addCommand.getFileList());

        return fortuneBillModel.getBillId();
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

    /**
     * 修改账单 - 真正的更新操作，而不是删除+新增
     */
    @Transactional(rollbackFor = Exception.class)
    public void modify(FortuneBillModifyCommand modifyCommand) {
        // 1. 加载原账单
        FortuneBillModel originalBill = fortuneBillFactory.loadById(modifyCommand.getBillId());
        originalBill.checkBookId(modifyCommand.getBookId());

        // 构建策略执行上下文
        BillStrategyContext context = this.buildContext(originalBill);

        // 获取对应策略并执行
        BillProcessStrategy strategy = strategyFactory.getStrategy(originalBill.getBillType());

        // 2. 如果原账单已确认，需要先回滚账户余额
        if (originalBill.getConfirm() && Objects.nonNull(originalBill.getAccountId()) && Objects.nonNull(originalBill.getAmount())) {
            strategy.refuseBalance(context);
        }

        // 3. 更新账单主体信息
        originalBill.loadModifyCommand(modifyCommand);

        // 4. 交易对象校验
        if (Objects.nonNull(modifyCommand.getPayeeId())) {
            FortunePayeeModel payee = fortunePayeeFactory.loadById(modifyCommand.getPayeeId());
            originalBill.checkPayeeExist(payee);
            originalBill.checkPayeeEnable(payee);
        }

        // 汇率转换
        strategy.convertRate(context);

        // 资金操作
        strategy.confirmBalance(context);

        // 7. 更新账单主记录
        originalBill.updateById();

        // 8. 更新分类关联（先删除旧的，再添加新的）
        fortuneCategoryRelationRepo.phyRemoveByBillId(modifyCommand.getBillId());
        this.processCategoryRelations(modifyCommand.getCategoryAmountPair(), originalBill);

        // 9. 更新标签关联（先删除旧的(物理删除)，再添加新的）
        fortuneTagRelationRepo.phyRemoveByBillId(modifyCommand.getBillId());
        if (CollectionUtils.isNotEmpty(modifyCommand.getTagIdList())) {
            this.processTagRelations(modifyCommand.getTagIdList(), originalBill);
        }

        // 10. 更新文件附件（先删除旧的，再添加新的）
        fortuneFileService.phyRemoveByBillId(modifyCommand.getBillId());
        fortuneFileService.batchAdd(modifyCommand.getBillId(), modifyCommand.getFileList());


    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        // 账户金额回滚
        this.refundBalance(fortuneBillModel);
        // 删除标签
        fortuneTagRelationRepo.removeByBillId(billId);
        // 删除分类
        fortuneCategoryRelationRepo.removeByBillId(billId);
        // 删除账单
        fortuneBillModel.deleteById();
        // 删除账单附件
        fortuneFileRepo.removeByBillId(billId);
    }

    /**
     * 构建策略执行上下文
     */
    private BillStrategyContext buildContext(FortuneBillModel bill) {
        BillStrategyContext context = new BillStrategyContext();

        context.setBillModel(bill);

        // 获取账本信息以确定默认币种
        FortuneBookModel bookModel = fortuneBookFactory.loadById(bill.getBookId());
        context.setBookModel(bookModel);

        // 根据账单类型设置相关账户
        context.setFromAccount(fortuneAccountFactory.loadById(bill.getAccountId()));

        if (Objects.nonNull(bill.getToAccountId())) {
            context.setToAccount(fortuneAccountFactory.loadById(bill.getToAccountId()));
        }

        return context;
    }

    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(FortuneBillModel bill) {
        // 验证退款基本条件
        if (!(bill.getConfirm() && Objects.nonNull(bill.getAccountId()) && Objects.nonNull(bill.getAmount()))) {
            log.warn("Invalid refund request for bill: {}", bill.getBillId());
            return;
        }
        // 构建策略执行上下文
        BillStrategyContext context = this.buildContext(bill);
        // 获取对应策略并执行
        BillProcessStrategy strategy = strategyFactory.getStrategy(bill.getBillType());
        // 回滚金额
        strategy.refuseBalance(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByBookId(Long bookId) {
        List<FortuneBillEntity> billList = fortuneBillRepo.getByBookId(bookId);
        if (CollectionUtils.isEmpty(billList)) {
            return;
        }
        List<Long> billIds = billList.stream().map(FortuneBillEntity::getBillId).toList();
        fortuneBillRepo.removeBatchByIds(billIds);
        fortuneCategoryRelationRepo.removeByBillIds(billIds);
        fortuneTagRelationRepo.removeByBillIds(billIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeByBookIds(List<Long> bookIds) {
        List<FortuneBillEntity> billList = fortuneBillRepo.getByBookIds(bookIds);
        List<Long> billIds = billList.stream().map(FortuneBillEntity::getBillId).toList();
        fortuneBillRepo.removeBatchByIds(billIds);
        fortuneCategoryRelationRepo.removeByBillIds(billIds);
        fortuneTagRelationRepo.removeByBillIds(billIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long bookId, Long billId) {
        FortuneBillModel fortuneBillModel = fortuneBillFactory.loadById(billId);
        fortuneBillModel.checkBookId(bookId);
        fortuneBillModel.setConfirm(Boolean.TRUE);

        // 获取对应策略并执行
        BillStrategyContext context = this.buildContext(fortuneBillModel);
        BillProcessStrategy strategy = strategyFactory.getStrategy(fortuneBillModel.getBillType());
        strategy.confirmBalance(context);

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
        return fortuneBillRepo.getBillStatistics(query);
    }

    public List<FortuneLineVo> getExpenseTrends(BillTrendsQuery billTrendsQuery) {
        List<FortuneLineVo> originData = fortuneBillRepo.getExpenseTrends(billTrendsQuery);
        return this.completeTimeSeries(originData, billTrendsQuery.getTimeGranularity(), billTrendsQuery.getTimePoint());
    }

    public List<FortuneLineVo> getIncomeTrends(BillTrendsQuery billTrendsQuery) {
        List<FortuneLineVo> originData = fortuneBillRepo.getIncomeTrends(billTrendsQuery);
        return this.completeTimeSeries(originData, billTrendsQuery.getTimeGranularity(), billTrendsQuery.getTimePoint());
    }


    private List<FortuneLineVo> completeTimeSeries(List<FortuneLineVo> originData, int timeGranularity, LocalDateTime baseTime) {
        // 参数校验
        if (CollectionUtils.isEmpty(originData)) {
            originData = new ArrayList<>();
        }
        if (Objects.isNull(baseTime)) {
            baseTime = LocalDateTime.now();
        }

        // 根据粒度类型生成对应时间序列
        List<String> timeKeys = this.generateTimeKeys(timeGranularity, baseTime, originData);

        // 构建数据映射表
        Map<String, FortuneLineVo> dataMap = originData.stream()
                .filter(vo -> StringUtils.isNotBlank(vo.getName()))
                .collect(Collectors.toMap(FortuneLineVo::getName, Function.identity()));

        // 补全缺失数据
        return timeKeys.stream().map(key -> dataMap.containsKey(key) ? dataMap.get(key) : new FortuneLineVo(key, BigDecimal.ZERO)).toList();
    }

    private List<String> generateTimeKeys(int granularity, LocalDateTime baseTime, List<FortuneLineVo> data) {
        LocalDate baseDate = baseTime.toLocalDate();

        switch (granularity) {
            // 过去7天（包含当天）
            case 1:
                return IntStream.rangeClosed(0, 6).mapToObj(offset -> baseDate.minusDays(6 - offset)).map(DAY_FORMATTER::format).toList();
            // 过去30天
            case 2:
                return IntStream.rangeClosed(0, 29).mapToObj(offset -> baseDate.minusDays(29 - offset)).map(DAY_FORMATTER::format).toList();
            // 过去12个月（自然月）
            case 3: {
                YearMonth currentMonth = YearMonth.from(baseTime);
                return IntStream.rangeClosed(0, 11).mapToObj(i -> currentMonth.minusMonths(11 - i)).map(month -> month.format(MONTH_FORMATTER)).toList();
            }
            // 跨年补全
            case 4: {
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
                        }).min()
                        // 无数据时使用当前年
                        .orElse(currentYear);
                return IntStream.rangeClosed(minYear, currentYear).mapToObj(year -> Year.of(year).format(YEAR_FORMATTER)).toList();
            }

            default:
                throw new IllegalArgumentException("Invalid time granularity: " + granularity);
        }
    }

    public List<FortunePieVo> getCategoryExpense(CategoryIncludeQuery query) {
        return fortuneBillRepo.getCategoryInclude(CategoryTypeEnum.EXPENSE, query);
    }

    public List<FortunePieVo> getCategoryIncome(CategoryIncludeQuery query) {
        return fortuneBillRepo.getCategoryInclude(CategoryTypeEnum.INCOME, query);
    }

    public List<FortuneBarVo> getTagExpense(TagIncludeQuery query) {
        return fortuneBillRepo.getTagInclude(CategoryTypeEnum.EXPENSE, query);
    }

    public List<FortuneBarVo> getTagIncome(TagIncludeQuery query) {
        return fortuneBillRepo.getTagInclude(CategoryTypeEnum.INCOME, query);
    }

    public List<FortunePieVo> getPayeeExpense(PayeeIncludeQuery query) {
        return fortuneBillRepo.getPayeeInclude(CategoryTypeEnum.EXPENSE, query);
    }

    public List<FortunePieVo> getPayeeIncome(PayeeIncludeQuery query) {
        return fortuneBillRepo.getPayeeInclude(CategoryTypeEnum.INCOME, query);
    }
}
