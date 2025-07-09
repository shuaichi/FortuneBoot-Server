package com.fortuneboot.rest.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.common.enums.fortune.RecoveryStrategyEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneRecurringBillRuleModifyCommand;
import com.fortuneboot.domain.common.vo.SelectOptionsVo;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillRuleEntity;
import com.fortuneboot.domain.query.fortune.FortuneRecurringBillRuleQuery;
import com.fortuneboot.domain.vo.fortune.FortuneRecurringBillLogVo;
import com.fortuneboot.domain.vo.fortune.FortuneRecurringBillRuleVo;
import com.fortuneboot.service.fortune.FortuneRecurringBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 周期记账Rest接口
 *
 * @author zhangchi118
 * @date 2025/7/4 19:28
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/recurring/bill")
@Tag(name = "周期记账API", description = "周期记账的增删查改")
public class FortuneRecurringBillController {

    private final FortuneRecurringBillService fortuneRecurringBillService;

    @Operation(summary = "查询周期记账规则")
    @GetMapping("/getRulePage")
    @PreAuthorize("@fortune.bookVisitorPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneRecurringBillRuleVo>> getRulePage(@Valid FortuneRecurringBillRuleQuery query) {
        IPage<FortuneRecurringBillRuleEntity> page = fortuneRecurringBillService.getRulePage(query);
        List<FortuneRecurringBillRuleVo> records = page.getRecords().stream().map(FortuneRecurringBillRuleVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "查询补偿策略")
    @GetMapping("/getRecoveryStrategy")
    public ResponseDTO<List<SelectOptionsVo>> getRecoveryStrategy() {
        List<SelectOptionsVo> result = Arrays.stream(RecoveryStrategyEnum.values())
                .map(item -> new SelectOptionsVo(item.getValue().longValue(), item.getDescription()))
                .toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "校验Cron表达式是否合法")
    @PostMapping("/checkCronExpression")
    public ResponseDTO<Boolean> checkCronExpression(String cronExpression) {
        return ResponseDTO.ok(fortuneRecurringBillService.checkCronExpression(cronExpression));
    }

    @Operation(summary = "新增周期记账规则")
    @PostMapping("/addRule")
    @AccessLog(title = "好记-周期记账规则", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<Boolean> addRule(@RequestBody @Valid FortuneRecurringBillRuleAddCommand command) {
        fortuneRecurringBillService.addNewRule(command);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "修改周期记账规则")
    @PutMapping("/modifyRule")
    @AccessLog(title = "好记-周期记账规则", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.bookActorPermission(#command.getBookId())")
    public ResponseDTO<Boolean> modifyRule(@RequestBody @Valid FortuneRecurringBillRuleModifyCommand command) {
        fortuneRecurringBillService.modifyRule(command);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "删除周期记账规则")
    @DeleteMapping("/{bookId}/{ruleId}/removeRule")
    @AccessLog(title = "好记-周期记账规则", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@fortune.bookActorPermission(#bookId)")
    public ResponseDTO<Boolean> removeRule(@PathVariable @Positive Long bookId, @PathVariable @Positive Long ruleId) {
        fortuneRecurringBillService.removeRule(bookId, ruleId);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "查询周期记账执行情况")
    @GetMapping("/{bookId}/{ruleId}/getLogByRuleId")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<List<FortuneRecurringBillLogVo>> getLogByRuleId(@PathVariable @Positive Long bookId, @PathVariable @Positive Long ruleId) {
        List<FortuneRecurringBillLogEntity> list = fortuneRecurringBillService.getLogByRuleId(ruleId);
        List<FortuneRecurringBillLogVo> result = list.stream().map(FortuneRecurringBillLogVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "启用周期记账")
    @PatchMapping("/{bookId}/{ruleId}/enableRule")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<Boolean> enableRule(@PathVariable @Positive Long bookId, @PathVariable @Positive Long ruleId) {
        fortuneRecurringBillService.enableRule(bookId, ruleId);
        return ResponseDTO.ok(Boolean.TRUE);
    }

    @Operation(summary = "禁用周期记账")
    @PatchMapping("/{bookId}/{ruleId}/disableRule")
    @PreAuthorize("@fortune.bookVisitorPermission(#bookId)")
    public ResponseDTO<Boolean> disableRule(@PathVariable @Positive Long bookId, @PathVariable @Positive Long ruleId) {
        fortuneRecurringBillService.disableRule(bookId, ruleId);
        return ResponseDTO.ok(Boolean.TRUE);
    }
}
