package com.fortuneboot.service.fortune;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.common.utils.poi.CustomExcelUtil;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.repository.fortune.FortuneBalanceSnapshotRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneBookRepo;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepo;
import com.fortuneboot.repository.system.SysConfigRepo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 统计聚合服务
 *
 * @author zhangchi118
 * @date 2026/8/4 20:18
 **/
@Service
@RequiredArgsConstructor
public class FortuneIncludeService {

    private static final int PERIOD_MONTH = 1;
    private static final int PERIOD_YEAR = 2;
    private static final int PERIOD_CUSTOM = 3;
    private static final int PERIOD_LAST_12_MONTHS = 3;
    private static final int PERIOD_LAST_5_YEARS = 4;
    private static final int DEFAULT_TOP_N = 10;
    private static final int MAX_TOP_N = 100;
    private static final int MAX_DAILY_RANGE_DAYS = 366;
    private static final int MAX_MONTH_RANGE = 60;
    private static final int MAX_YEAR_RANGE = 10;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final FortuneBillService fortuneBillService;
    private final FortuneAccountService fortuneAccountService;
    private final FortuneBillRepo fortuneBillRepo;
    private final FortuneBookRepo fortuneBookRepo;
    private final FortuneFinanceOrderRepo fortuneFinanceOrderRepo;
    private final FortuneBalanceSnapshotRepo fortuneBalanceSnapshotRepo;
    private final SysConfigRepo sysConfigRepo;

    public DashboardVo getDashboard(DashboardQuery query) {
        DateRange period = resolveDashboardRange(query);
        DateRange previous = resolvePreviousRange(period, query.getPeriodType());
        Long groupId = getGroupId(query.getBookId());

        BillIncludeQuery periodQuery = new BillIncludeQuery();
        periodQuery.setBookId(query.getBookId());
        periodQuery.setStartDate(period.startDate());
        periodQuery.setEndDate(period.endDate());
        periodQuery.setInclude(Boolean.TRUE);

        FortuneBillQuery billQuery = toFortuneBillQuery(periodQuery);
        BillStatisticsVo periodStatistics = normalize(fortuneBillService.getBillStatistics(billQuery));
        BillStatisticsVo previousStatistics = normalize(fortuneBillService.getBillStatistics(toFortuneBillQuery(periodQuery, previous)));
        FortuneAssetsLiabilitiesVo assetsLiabilities = fortuneAccountService.getFortuneAssetsLiabilities(groupId);
        LoanOverviewVo loanOverview = getLoanOverview(query.getBookId());

        DashboardVo vo = new DashboardVo();
        vo.setPeriod(periodStatistics);
        vo.setPrevious(previousStatistics);
        vo.setRingIncomeRate(rate(periodStatistics.getIncome(), previousStatistics.getIncome()));
        vo.setRingExpenseRate(rate(periodStatistics.getExpense(), previousStatistics.getExpense()));
        vo.setTotalAssets(nullToZero(assetsLiabilities.getTotalAssets()));
        vo.setTotalLiabilities(nullToZero(assetsLiabilities.getTotalLiabilities()));
        vo.setNetAssets(nullToZero(assetsLiabilities.getNetAssets()));
        vo.setAvgDailyExpense(avgDailyExpense(periodStatistics.getExpense(), period));
        vo.setMaxSingleExpense(nullToZero(fortuneBillRepo.getMaxSingleExpense(periodQuery)));
        vo.setUnconfirmedCount(Objects.requireNonNullElse(fortuneBillRepo.countUnconfirmedBills(periodQuery), 0));
        vo.setPendingReceivable(loanOverview.getReceivableCount());
        vo.setRecentTrend(getRecentExpenseTrend(query.getBookId()));
        return vo;
    }

    public List<FortuneLineVo> getDateInclude(BillIncludeQuery query) {
        BillIncludeQuery normalized = normalizeBillQuery(query, BillTypeEnum.EXPENSE.getValue());
        DateRange range = resolveQueryRange(normalized);
        normalized.setStartDate(range.startDate());
        normalized.setEndDate(range.endDate());
        return completeDailySeries(fortuneBillRepo.getDateInclude(normalized), range.startDate(), range.endDate());
    }

    public List<FortuneBarVo> getMemberInclude(BillIncludeQuery query) {
        BillIncludeQuery normalized = normalizeBillQuery(query, BillTypeEnum.EXPENSE.getValue());
        return fortuneBillRepo.getMemberInclude(normalized);
    }

    public LoanOverviewVo getLoanOverview(Long bookId) {
        List<LoanDetailVo> receivables = fortuneBillRepo.getLoanReceivableDetails(bookId);
        List<LoanDetailVo> payables = fortuneBillRepo.getLoanPayableDetails(bookId);

        LoanOverviewVo vo = new LoanOverviewVo();
        vo.setTotalReceivable(sumLoanAmount(receivables));
        vo.setTotalPayable(sumLoanAmount(payables));
        vo.setReceivableCount(receivables.size());
        vo.setPayableCount(payables.size());
        vo.setTopReceivables(receivables.stream().limit(DEFAULT_TOP_N).toList());
        return vo;
    }

    public List<BillCompareVo> getBillCompare(BillCompareQuery query) {
        query.setCompareType(Objects.requireNonNullElse(query.getCompareType(), 1));
        validateCompareRange(query);
        query.setInclude(Boolean.TRUE);
        return completeCompareSeries(fortuneBillRepo.getBillCompare(query), query);
    }

    public List<FortuneBarVo> getBillRank(BillRankQuery query) {
        query.setBillType(Objects.requireNonNullElse(query.getBillType(), BillTypeEnum.EXPENSE.getValue()));
        query.setInclude(Boolean.TRUE);
        query.setTopN(normalizeTopN(query.getTopN()));
        return fortuneBillRepo.getBillRank(query);
    }

    public List<HeatmapVo> getCalendarHeatmap(CalendarHeatmapQuery query) {
        query.setBillType(Objects.requireNonNullElse(query.getBillType(), BillTypeEnum.EXPENSE.getValue()));
        query.setInclude(Boolean.TRUE);
        Map<String, HeatmapVo> origin = fortuneBillRepo.getCalendarHeatmap(query).stream()
                .collect(Collectors.toMap(HeatmapVo::getDate, Function.identity()));
        LocalDate start = LocalDate.of(query.getYear(), 1, 1);
        LocalDate end = LocalDate.of(query.getYear(), 12, 31);
        return start.datesUntil(end.plusDays(1)).map(date -> {
            String key = DAY_FORMATTER.format(date);
            HeatmapVo vo = origin.get(key);
            if (vo != null) {
                return vo;
            }
            HeatmapVo empty = new HeatmapVo();
            empty.setDate(key);
            empty.setAmount(BigDecimal.ZERO);
            empty.setCount(0);
            return empty;
        }).toList();
    }

    public List<AccountIncludeVo> getAccountInclude(BillIncludeQuery query) {
        BillIncludeQuery normalized = normalizeBillQuery(query, BillTypeEnum.EXPENSE.getValue());
        return fortuneBillRepo.getAccountInclude(normalized);
    }

    public List<BillTypeDistributionVo> getBillTypeDistribution(BillIncludeQuery query) {
        BillIncludeQuery normalized = normalizeBillQuery(query, null);
        return fortuneBillRepo.getBillTypeDistribution(normalized).stream().peek(vo ->
                vo.setName(BillTypeEnum.getDescByValue(vo.getBillType()))
        ).toList();
    }

    public List<FortuneLineVo> getNetAssetsTrend(Long groupId, NetAssetsTrendQuery query) {
        Integer periodType = normalizeTrendPeriod(query.getPeriodType());
        return completePeriodSeries(fortuneBalanceSnapshotRepo.getNetAssetsTrend(groupId, periodType), periodType);
    }

    public List<FortuneLineVo> getAccountBalanceTrend(AccountBalanceTrendQuery query) {
        Integer periodType = normalizeTrendPeriod(query.getPeriodType());
        return completePeriodSeries(fortuneBalanceSnapshotRepo.getAccountBalanceTrend(query.getAccountId(), periodType), periodType);
    }

    public List<FinanceProfitVo> getFinanceProfit(Long bookId, FinanceProfitQuery query) {
        return fortuneFinanceOrderRepo.getFinanceProfit(bookId, query);
    }

    public IncludePolicyVo getIncludePolicy() {
        IncludePolicyVo vo = new IncludePolicyVo();
        vo.setExcludeTransferFromExpense(getBooleanConfig("fortune.include.excludeTransferFromExpense", Boolean.TRUE));
        vo.setExcludeLoanFromExpense(getBooleanConfig("fortune.include.excludeLoanFromExpense", Boolean.TRUE));
        vo.setIncludeUnconfirmed(getBooleanConfig("fortune.include.includeUnconfirmed", Boolean.TRUE));
        return vo;
    }

    public void exportReport(HttpServletResponse response, ReportExportQuery query) {
        query.setInclude(Boolean.TRUE);
        List<ReportExportRowVo> rows = new ArrayList<>();
        BillStatisticsVo statistics = normalize(fortuneBillService.getBillStatistics(toFortuneBillQuery(query)));
        rows.add(row("收支汇总", "收入", statistics.getIncome(), null, null, null));
        rows.add(row("收支汇总", "支出", null, statistics.getExpense(), null, null));
        rows.add(row("收支汇总", "结余", null, null, statistics.getSurplus(), null));

        getBillCompare(toCompareQuery(query)).forEach(item -> {
            ReportExportRowVo row = row("收支对比", item.getName(), item.getIncome(), item.getExpense(), null, null);
            rows.add(row);
        });
        getBillRank(toRankQuery(query)).forEach(item -> rows.add(row("TopN排行", item.getName(), null, null, item.getValue(), null)));
        getBillTypeDistribution(query).forEach(item -> rows.add(row("类型分布", item.getName(), null, null, item.getValue(), item.getPercent())));
        CustomExcelUtil.writeToResponse(rows, ReportExportRowVo.class, response);
    }

    private DateRange resolveDashboardRange(DashboardQuery query) {
        Integer periodType = Objects.requireNonNullElse(query.getPeriodType(), PERIOD_MONTH);
        LocalDate now = LocalDate.now();
        DateRange range;
        if (Objects.equals(periodType, PERIOD_YEAR)) {
            range = new DateRange(now.withDayOfYear(1), now);
        } else if (Objects.equals(periodType, PERIOD_CUSTOM)) {
            range = new DateRange(Objects.requireNonNullElse(query.getStartDate(), now.withDayOfMonth(1)),
                    Objects.requireNonNullElse(query.getEndDate(), now));
        } else {
            range = new DateRange(now.withDayOfMonth(1), now);
        }
        return validateRange(range);
    }

    private DateRange resolvePreviousRange(DateRange range, Integer periodType) {
        if (Objects.equals(periodType, PERIOD_YEAR)) {
            return new DateRange(range.startDate().minusYears(1), range.endDate().minusYears(1));
        }
        if (Objects.equals(periodType, PERIOD_CUSTOM)) {
            long days = range.endDate().toEpochDay() - range.startDate().toEpochDay() + 1;
            LocalDate end = range.startDate().minusDays(1);
            return new DateRange(end.minusDays(days - 1), end);
        }
        YearMonth previousMonth = YearMonth.from(range.startDate()).minusMonths(1);
        return new DateRange(previousMonth.atDay(1), previousMonth.atEndOfMonth());
    }

    private DateRange resolveQueryRange(BillIncludeQuery query) {
        LocalDate now = LocalDate.now();
        return validateRange(new DateRange(Objects.requireNonNullElse(query.getStartDate(), now.withDayOfMonth(1)),
                Objects.requireNonNullElse(query.getEndDate(), now)));
    }

    private DateRange validateRange(DateRange range) {
        if (range.endDate().isBefore(range.startDate())) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "结束日期不能早于开始日期");
        }
        long days = range.endDate().toEpochDay() - range.startDate().toEpochDay() + 1;
        if (days > MAX_DAILY_RANGE_DAYS) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "统计日期跨度不能超过" + MAX_DAILY_RANGE_DAYS + "天");
        }
        return range;
    }

    private void validateCompareRange(BillCompareQuery query) {
        if (query.getStartDate() == null || query.getEndDate() == null) {
            return;
        }
        validateRange(new DateRange(query.getStartDate(), query.getEndDate()));
        if (Objects.equals(query.getCompareType(), 2)) {
            int years = query.getEndDate().getYear() - query.getStartDate().getYear() + 1;
            if (years > MAX_YEAR_RANGE) {
                throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "按年对比跨度不能超过" + MAX_YEAR_RANGE + "年");
            }
            return;
        }
        int months = (query.getEndDate().getYear() - query.getStartDate().getYear()) * 12
                + query.getEndDate().getMonthValue() - query.getStartDate().getMonthValue() + 1;
        if (months > MAX_MONTH_RANGE) {
            throw new ApiException(ErrorCode.Business.COMMON_UNSUPPORTED_OPERATION, "按月对比跨度不能超过" + MAX_MONTH_RANGE + "个月");
        }
    }

    private BillIncludeQuery normalizeBillQuery(BillIncludeQuery query, Integer defaultBillType) {
        query.setInclude(Boolean.TRUE);
        if (defaultBillType != null && query.getBillType() == null) {
            query.setBillType(defaultBillType);
        }
        return query;
    }

    private FortuneBillQuery toFortuneBillQuery(BillIncludeQuery query) {
        DateRange range = resolveQueryRange(query);
        return toFortuneBillQuery(query, range);
    }

    private FortuneBillQuery toFortuneBillQuery(BillIncludeQuery query, DateRange range) {
        FortuneBillQuery billQuery = new FortuneBillQuery();
        billQuery.setBookId(query.getBookId());
        billQuery.setBillType(query.getBillType());
        billQuery.setTitle(query.getTitle());
        billQuery.setConfirm(query.getConfirm());
        billQuery.setInclude(Boolean.TRUE);
        billQuery.setTradeTimeStartTime(DAY_FORMATTER.format(range.startDate()));
        billQuery.setTradeTimeEndTime(DAY_FORMATTER.format(range.endDate()));
        return billQuery;
    }

    private List<FortuneLineVo> getRecentExpenseTrend(Long bookId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        BillIncludeQuery query = new BillIncludeQuery();
        query.setBookId(bookId);
        query.setStartDate(start);
        query.setEndDate(end);
        query.setBillType(BillTypeEnum.EXPENSE.getValue());
        query.setInclude(Boolean.TRUE);
        return completeDailySeries(fortuneBillRepo.getDateInclude(query), start, end);
    }

    private List<FortuneLineVo> completeDailySeries(List<FortuneLineVo> originData, LocalDate startDate, LocalDate endDate) {
        Map<String, FortuneLineVo> dataMap = originData.stream()
                .filter(vo -> StringUtils.isNotBlank(vo.getName()))
                .collect(Collectors.toMap(FortuneLineVo::getName, Function.identity()));
        return startDate.datesUntil(endDate.plusDays(1)).map(date -> {
            String key = DAY_FORMATTER.format(date);
            return dataMap.getOrDefault(key, new FortuneLineVo(key, BigDecimal.ZERO));
        }).toList();
    }

    private List<BillCompareVo> completeCompareSeries(List<BillCompareVo> originData, BillCompareQuery query) {
        if (query.getStartDate() == null || query.getEndDate() == null) {
            return originData;
        }
        Map<String, BillCompareVo> dataMap = originData.stream().collect(Collectors.toMap(BillCompareVo::getName, Function.identity()));
        if (Objects.equals(query.getCompareType(), 2)) {
            return IntStream.rangeClosed(query.getStartDate().getYear(), query.getEndDate().getYear()).mapToObj(year ->
                    dataMap.getOrDefault(String.valueOf(year), emptyCompare(String.valueOf(year)))
            ).toList();
        }
        YearMonth start = YearMonth.from(query.getStartDate());
        YearMonth end = YearMonth.from(query.getEndDate());
        List<BillCompareVo> result = new ArrayList<>();
        for (YearMonth cursor = start; !cursor.isAfter(end); cursor = cursor.plusMonths(1)) {
            String key = cursor.format(MONTH_FORMATTER);
            result.add(dataMap.getOrDefault(key, emptyCompare(key)));
        }
        return result;
    }

    private BillCompareVo emptyCompare(String name) {
        BillCompareVo vo = new BillCompareVo();
        vo.setName(name);
        vo.setIncome(BigDecimal.ZERO);
        vo.setExpense(BigDecimal.ZERO);
        return vo;
    }

    private List<FortuneLineVo> completePeriodSeries(List<FortuneLineVo> originData, Integer periodType) {
        Map<String, FortuneLineVo> dataMap = originData.stream()
                .filter(vo -> StringUtils.isNotBlank(vo.getName()))
                .collect(Collectors.toMap(FortuneLineVo::getName, Function.identity(), (left, right) -> right));
        if (Objects.equals(periodType, PERIOD_LAST_5_YEARS)) {
            int currentYear = LocalDate.now().getYear();
            return IntStream.rangeClosed(currentYear - 4, currentYear).mapToObj(year -> {
                String key = String.valueOf(year);
                return dataMap.getOrDefault(key, new FortuneLineVo(key, BigDecimal.ZERO));
            }).toList();
        }
        YearMonth current = YearMonth.now();
        return IntStream.rangeClosed(0, 11).mapToObj(i -> {
            String key = current.minusMonths(11L - i).format(MONTH_FORMATTER);
            return dataMap.getOrDefault(key, new FortuneLineVo(key, BigDecimal.ZERO));
        }).toList();
    }

    private BillStatisticsVo normalize(BillStatisticsVo vo) {
        if (vo == null) {
            vo = new BillStatisticsVo();
        }
        vo.setIncome(nullToZero(vo.getIncome()));
        vo.setExpense(nullToZero(vo.getExpense()));
        vo.setSurplus(nullToZero(vo.getSurplus()));
        return vo;
    }

    private BigDecimal avgDailyExpense(BigDecimal expense, DateRange range) {
        long days = range.endDate().toEpochDay() - range.startDate().toEpochDay() + 1;
        return nullToZero(expense).divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(BigDecimal current, BigDecimal previous) {
        BigDecimal previousValue = nullToZero(previous);
        if (previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return nullToZero(current).subtract(previousValue)
                .divide(previousValue, 4, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumLoanAmount(List<LoanDetailVo> details) {
        if (CollectionUtils.isEmpty(details)) {
            return BigDecimal.ZERO;
        }
        return details.stream().map(LoanDetailVo::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer normalizeTopN(Integer topN) {
        if (topN == null || topN <= 0) {
            return DEFAULT_TOP_N;
        }
        return Math.min(topN, MAX_TOP_N);
    }

    private Integer normalizeTrendPeriod(Integer periodType) {
        return Objects.equals(periodType, PERIOD_LAST_5_YEARS) ? PERIOD_LAST_5_YEARS : PERIOD_LAST_12_MONTHS;
    }

    private Long getGroupId(Long bookId) {
        FortuneBookEntity book = fortuneBookRepo.getById(bookId);
        return book.getGroupId();
    }

    private Boolean getBooleanConfig(String key, Boolean defaultValue) {
        String value = sysConfigRepo.getConfigValueByKey(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private String safeExcelText(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String trimmed = text.stripLeading();
        if (trimmed.startsWith("=") || trimmed.startsWith("+") || trimmed.startsWith("-") || trimmed.startsWith("@")) {
            return "'" + text;
        }
        return text.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", StringUtils.EMPTY);
    }

    private ReportExportRowVo row(String section, String name, BigDecimal income, BigDecimal expense, BigDecimal amount, BigDecimal percent) {
        ReportExportRowVo row = new ReportExportRowVo();
        row.setSection(safeExcelText(section));
        row.setName(safeExcelText(name));
        row.setIncome(income);
        row.setExpense(expense);
        row.setAmount(amount);
        row.setPercent(percent);
        return row;
    }

    private BillCompareQuery toCompareQuery(ReportExportQuery query) {
        BillCompareQuery compareQuery = new BillCompareQuery();
        copyBaseQuery(query, compareQuery);
        compareQuery.setCompareType(Objects.equals(query.getReportType(), 2) ? 2 : 1);
        return compareQuery;
    }

    private BillRankQuery toRankQuery(ReportExportQuery query) {
        BillRankQuery rankQuery = new BillRankQuery();
        copyBaseQuery(query, rankQuery);
        rankQuery.setBillType(BillTypeEnum.EXPENSE.getValue());
        rankQuery.setTopN(DEFAULT_TOP_N);
        return rankQuery;
    }

    private void copyBaseQuery(BillIncludeQuery source, BillIncludeQuery target) {
        target.setBookId(source.getBookId());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setTitle(source.getTitle());
        target.setBillType(source.getBillType());
        target.setCategoryIds(source.getCategoryIds());
        target.setTagIds(source.getTagIds());
        target.setPayeeIds(source.getPayeeIds());
        target.setAccountIds(source.getAccountIds());
        target.setMemberIds(source.getMemberIds());
        target.setConfirm(source.getConfirm());
        target.setInclude(source.getInclude());
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
