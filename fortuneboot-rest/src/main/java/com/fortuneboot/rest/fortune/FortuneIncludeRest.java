package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.infrastructure.annotations.ratelimit.RateLimit;
import com.fortuneboot.infrastructure.annotations.ratelimit.RateLimit.CacheType;
import com.fortuneboot.infrastructure.annotations.ratelimit.RateLimit.LimitType;
import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.service.fortune.FortuneAccountService;
import com.fortuneboot.service.fortune.FortuneBillService;
import com.fortuneboot.service.fortune.FortuneIncludeService;
import com.fortuneboot.service.system.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 统计
 * @author zhangchi118
 * @date 2025/2/22 22:19
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/include")
@Tag(name = "统计", description = "统计各种指标")
public class FortuneIncludeRest {

    private final FortuneBillService fortuneBillService;

    private final FortuneAccountService fortuneAccountService;

    private final FortuneIncludeService fortuneIncludeService;

    private final UserApplicationService userApplicationService;


    @Operation(summary = "统计支出收入")
    @GetMapping("/getBillStatistics")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<BillStatisticsVo> getBillStatistics(FortuneBillQuery query) {
        return ResponseDTO.ok(fortuneBillService.getBillStatistics(query));
    }

    @Operation(summary = "统计总资产")
    @GetMapping("/{groupId}/getTotalAssets")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<List<FortunePieVo>> getTotalAssets(@PathVariable @Positive Long groupId){
        return ResponseDTO.ok(fortuneAccountService.getTotalAssets(groupId));
    }

    @Operation(summary = "统计总负债")
    @GetMapping("/{groupId}/getTotalLiabilities")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<List<FortunePieVo>> getTotalLiabilities(@PathVariable @Positive Long groupId){
        return ResponseDTO.ok(fortuneAccountService.getTotalLiabilities(groupId));
    }


    @Operation(summary = "统计收入折线图")
    @GetMapping("/getIncomeTrends")
    @PreAuthorize("@fortune.bookVisitorPermission(#billTrendsQuery.getBookId())")
    public ResponseDTO<List<FortuneLineVo>> getIncomeTrends(BillTrendsQuery billTrendsQuery){
        billTrendsQuery.setBillType(BillTypeEnum.INCOME.getValue());
        return ResponseDTO.ok(fortuneBillService.getIncomeTrends(billTrendsQuery));
    }

    @Operation(summary = "统计支出折线图")
    @GetMapping("/getExpenseTrends")
    @PreAuthorize("@fortune.bookVisitorPermission(#billTrendsQuery.getBookId())")
    public ResponseDTO<List<FortuneLineVo>> getExpenseTrends(BillTrendsQuery billTrendsQuery){
        billTrendsQuery.setBillType(BillTypeEnum.EXPENSE.getValue());
        return ResponseDTO.ok(fortuneBillService.getExpenseTrends(billTrendsQuery));
    }

    @Operation(summary = "统计资产负债")
    @GetMapping("/{groupId}/getFortuneAssetsLiabilities")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<FortuneAssetsLiabilitiesVo> getFortuneAssetsLiabilities(@PathVariable @Positive Long groupId){
        return ResponseDTO.ok(fortuneAccountService.getFortuneAssetsLiabilities(groupId));
    }

    @Operation(summary = "统计支出分类")
    @GetMapping("/getCategoryExpense")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortunePieVo>> getCategoryExpense(@Valid CategoryIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getCategoryExpense(query));
    }

    @Operation(summary = "统计收入分类")
    @GetMapping("/getCategoryIncome")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortunePieVo>> getCategoryIncome(@Valid CategoryIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getCategoryIncome(query));
    }

    @Operation(summary = "统计支出标签")
    @GetMapping("/getTagExpense")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneBarVo>> getTagExpense(@Valid TagIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getTagExpense(query));
    }

    @Operation(summary = "统计收入标签")
    @GetMapping("/getTagIncome")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortuneBarVo>> getTagIncome(@Valid TagIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getTagIncome(query));
    }


    @Operation(summary = "统计支出交易对象")
    @GetMapping("/getPayeeExpense")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortunePieVo>> getPayeeExpense(@Valid PayeeIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getPayeeExpense(query));
    }

    @Operation(summary = "统计收入交易对象")
    @GetMapping("/getPayeeIncome")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<List<FortunePieVo>> getPayeeIncome(@Valid PayeeIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getPayeeIncome(query));
    }

    @Operation(summary = "获取首页金额显示设置", description = "首页金额显示")
    @GetMapping("/getDisplayConfig")
    public ResponseDTO<String> getDisplayConfig(){
        return ResponseDTO.ok(userApplicationService.getDisplayConfig());
    }

    @Operation(summary = "首页聚合看板")
    @GetMapping("/dashboard")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<DashboardVo> dashboard(@Valid DashboardQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getDashboard(query));
    }

    @Operation(summary = "查询日期统计")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getDateInclude")
    public ResponseDTO<List<FortuneLineVo>> getDateInclude(@Valid BillIncludeQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getDateInclude(query));
    }

    @Operation(summary = "统计成员支出")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getMemberInclude")
    public ResponseDTO<List<FortuneBarVo>> getMemberInclude(@Valid BillIncludeQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getMemberInclude(query));
    }

    @Operation(summary = "借贷总览")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    @GetMapping("/{bookId}/getLoanOverview")
    public ResponseDTO<LoanOverviewVo> getLoanOverview(@PathVariable @Positive Long bookId) {
        return ResponseDTO.ok(fortuneIncludeService.getLoanOverview(bookId));
    }

    @Operation(summary = "收支对比")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getBillCompare")
    public ResponseDTO<List<BillCompareVo>> getBillCompare(@Valid BillCompareQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getBillCompare(query));
    }

    @Operation(summary = "收支排行")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getBillRank")
    public ResponseDTO<List<FortuneBarVo>> getBillRank(@Valid BillRankQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getBillRank(query));
    }

    @Operation(summary = "日历热力图")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getCalendarHeatmap")
    public ResponseDTO<List<HeatmapVo>> getCalendarHeatmap(@Valid CalendarHeatmapQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getCalendarHeatmap(query));
    }

    @Operation(summary = "账户维度统计")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getAccountInclude")
    public ResponseDTO<List<AccountIncludeVo>> getAccountInclude(@Valid BillIncludeQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getAccountInclude(query));
    }

    @Operation(summary = "账单类型分布")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/getBillTypeDistribution")
    public ResponseDTO<List<BillTypeDistributionVo>> getBillTypeDistribution(@Valid BillIncludeQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getBillTypeDistribution(query));
    }

    @Operation(summary = "账户类型资产分布")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    @GetMapping("/{groupId}/getAssetsByAccountType")
    public ResponseDTO<List<AccountTypeAssetsVo>> getAssetsByAccountType(@PathVariable @Positive Long groupId) {
        return ResponseDTO.ok(fortuneAccountService.getAssetsByAccountType(groupId));
    }

    @Operation(summary = "信用卡额度看板")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    @GetMapping("/{groupId}/getCreditCardOverview")
    public ResponseDTO<List<CreditCardVo>> getCreditCardOverview(@PathVariable @Positive Long groupId) {
        return ResponseDTO.ok(fortuneAccountService.getCreditCardOverview(groupId));
    }

    @Operation(summary = "净资产趋势")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    @GetMapping("/{groupId}/netAssetsTrend")
    public ResponseDTO<List<FortuneLineVo>> netAssetsTrend(@PathVariable @Positive Long groupId, NetAssetsTrendQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getNetAssetsTrend(groupId, query));
    }

    @Operation(summary = "账户余额趋势")
    @PreAuthorize("@fortune.accountVisitorPermission(#query.getAccountId())")
    @GetMapping("/getAccountBalanceTrend")
    public ResponseDTO<List<FortuneLineVo>> getAccountBalanceTrend(@Valid AccountBalanceTrendQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getAccountBalanceTrend(query));
    }

    @Operation(summary = "理财收益统计")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    @GetMapping("/{bookId}/getFinanceProfit")
    public ResponseDTO<List<FinanceProfitVo>> getFinanceProfit(@PathVariable @Positive Long bookId, FinanceProfitQuery query) {
        return ResponseDTO.ok(fortuneIncludeService.getFinanceProfit(bookId, query));
    }

    @Operation(summary = "统计口径说明")
    @GetMapping("/getIncludePolicy")
    public ResponseDTO<IncludePolicyVo> getIncludePolicy() {
        return ResponseDTO.ok(fortuneIncludeService.getIncludePolicy());
    }

    @Operation(summary = "导出统计报表")
    @AccessLog(title = "好记-统计报表", businessType = BusinessTypeEnum.EXPORT)
    @RateLimit(key = "Rate-Limit:Fortune-Report-Export:", time = 60, maxCount = 5, cacheType = CacheType.MEMORY,
            limitType = LimitType.SYSTEM_USER)
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    @GetMapping("/exportReport")
    public void exportReport(HttpServletResponse response, @Valid ReportExportQuery query) {
        fortuneIncludeService.exportReport(response, query);
    }
}
