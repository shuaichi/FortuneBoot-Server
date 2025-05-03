package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.service.fortune.FortuneAccountService;
import com.fortuneboot.service.fortune.FortuneBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
public class FortuneIncludeController {

    private final FortuneBillService fortuneBillService;

    private final FortuneAccountService fortuneAccountService;


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
    @PreAuthorize("@fortune.bookVisitorPermission(#billTrendsQuery.bookId)")
    public ResponseDTO<List<FortuneLineVo>> getIncomeTrends(@Param("billTrendsQuery") BillTrendsQuery billTrendsQuery){
        billTrendsQuery.setBillType(BillTypeEnum.INCOME.getValue());
        return ResponseDTO.ok(fortuneBillService.getIncomeTrends(billTrendsQuery));
    }

    @Operation(summary = "统计支出折线图")
    @GetMapping("/getExpenseTrends")
    @PreAuthorize("@fortune.bookVisitorPermission(#billTrendsQuery.bookId)")
    public ResponseDTO<List<FortuneLineVo>> getExpenseTrends(@Param("billTrendsQuery") BillTrendsQuery billTrendsQuery){
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
    @PreAuthorize("@fortune.bookVisitorPermission(#query.bookId)")
    public ResponseDTO<List<FortunePieVo>> getCategoryExpense(@Valid CategoryIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getCategoryExpense(query));
    }

    @Operation(summary = "统计收入分类")
    @GetMapping("/getCategoryIncome")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.bookId)")
    public ResponseDTO<List<FortunePieVo>> getCategoryIncome(@Valid CategoryIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getCategoryIncome(query));
    }

    @Operation(summary = "统计支出标签")
    @GetMapping("/getTagExpense")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.bookId)")
    public ResponseDTO<List<FortuneBarVo>> getTagExpense(@Valid TagIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getTagExpense(query));
    }

    @Operation(summary = "统计收入标签")
    @GetMapping("/getTagIncome")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.bookId)")
    public ResponseDTO<List<FortuneBarVo>> getTagIncome(@Valid TagIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getTagIncome(query));
    }


    @Operation(summary = "统计支出交易对象")
    @GetMapping("/getPayeeExpense")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.bookId)")
    public ResponseDTO<List<FortunePieVo>> getPayeeExpense(@Valid PayeeIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getPayeeExpense(query));
    }

    @Operation(summary = "统计收入交易对象")
    @GetMapping("/getPayeeIncome")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.bookId)")
    public ResponseDTO<List<FortunePieVo>> getPayeeIncome(@Valid PayeeIncludeQuery query){
        return ResponseDTO.ok(fortuneBillService.getPayeeIncome(query));
    }
}
