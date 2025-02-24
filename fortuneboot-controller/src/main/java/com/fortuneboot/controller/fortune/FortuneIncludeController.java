package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.vo.fortune.include.BillStatisticsVo;
import com.fortuneboot.domain.vo.fortune.include.FortuneAssetsLiabilitiesVo;
import com.fortuneboot.domain.vo.fortune.include.FortuneLineVo;
import com.fortuneboot.domain.vo.fortune.include.FortunePieVo;
import com.fortuneboot.service.fortune.FortuneAccountService;
import com.fortuneboot.service.fortune.FortuneBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/{bookId}/getBillStatistics")
    @AccessLog(title = "好记-账本管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.groupVisitorPermission(#bookId)")
    public ResponseDTO<BillStatisticsVo> getBillStatistics(@PathVariable @NotNull @Positive Long bookId) {
        return ResponseDTO.ok(fortuneBillService.getBillStatistics(bookId));
    }

    @Operation(summary = "统计总资产")
    @GetMapping("/{groupId}/getTotalAssets")
    @AccessLog(title = "好记-账本管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<List<FortunePieVo>> getTotalAssets(@PathVariable @NotNull @Positive Long groupId){
        return ResponseDTO.ok(fortuneAccountService.getTotalAssets(groupId));
    }

    @Operation(summary = "统计总负债")
    @GetMapping("/{groupId}/getTotalLiabilities")
    @AccessLog(title = "好记-账本管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<List<FortunePieVo>> getTotalLiabilities(@PathVariable @NotNull @Positive Long groupId){
        return ResponseDTO.ok(fortuneAccountService.getTotalLiabilities(groupId));
    }


    @Operation(summary = "统计收入折线图")
    @GetMapping("/{bookId}/getIncomeTrends")
    @AccessLog(title = "好记-账本管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<List<FortuneLineVo>> getIncomeTrends(@PathVariable @NotNull @Positive Long bookId){
        return ResponseDTO.ok(fortuneAccountService.getIncomeTrends(bookId));
    }

    @Operation(summary = "统计支出折线图")
    @GetMapping("/{bookId}/getExpenseTrends")
    @AccessLog(title = "好记-账本管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<List<FortuneLineVo>> getExpenseTrends(@PathVariable @NotNull @Positive Long bookId) {
        return ResponseDTO.ok(fortuneBillService.getExpenseTrends(bookId));
    }

    @Operation(summary = "")
    @GetMapping("/{groupId}/getFortuneAssetsLiabilities")
    @AccessLog(title = "好记-账本管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<FortuneAssetsLiabilitiesVo> getFortuneAssetsLiabilities(@PathVariable @NotNull @Positive Long groupId){
        return ResponseDTO.ok(fortuneAccountService.getFortuneAssetsLiabilities(groupId));
    }
}
