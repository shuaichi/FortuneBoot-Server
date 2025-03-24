package com.fortuneboot.rest.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.common.enums.common.BusinessTypeEnum;
import com.fortuneboot.customize.accessLog.AccessLog;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountAdjustCommand;
import com.fortuneboot.domain.command.fortune.FortuneAccountModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneAccountEntity;
import com.fortuneboot.domain.query.fortune.FortuneAccountQuery;
import com.fortuneboot.domain.vo.fortune.FortuneAccountVo;
import com.fortuneboot.service.fortune.FortuneAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/10 22:44
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/account")
@Tag(name = "账户API", description = "账户相关的增删查改")
public class FortuneAccountController {

    private final FortuneAccountService fortuneAccountService;

    @Operation(summary = "分页查询账户")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.groupVisitorPermission(#query.getGroupId())")
    public ResponseDTO<PageDTO<FortuneAccountVo>> getPage(@Valid FortuneAccountQuery query) {
        IPage<FortuneAccountEntity> page = fortuneAccountService.getPage(query);
        List<FortuneAccountVo> records = page.getRecords().stream().map(FortuneAccountVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "查询启用的账户")
    @GetMapping("/{groupId}/getEnableList")
    @PreAuthorize("@fortune.groupVisitorPermission(#groupId)")
    public ResponseDTO<List<FortuneAccountVo>> getEnableAccountList(@PathVariable @NotNull @Positive Long groupId){
        List<FortuneAccountEntity> list = fortuneAccountService.getEnableAccountList(groupId);
        List<FortuneAccountVo> result = list.stream().map(FortuneAccountVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增账户")
    @PostMapping("/add")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.groupActorPermission(#addCommand.getGroupId())")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneAccountAddCommand addCommand) {
        fortuneAccountService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改账户")
    @PutMapping("/modify")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.MODIFY)
    @PreAuthorize("@fortune.groupActorPermission(#modifyCommand.getGroupId())")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneAccountModifyCommand modifyCommand) {
        fortuneAccountService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "余额调整")
    @PatchMapping("/balanceAdjust")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.BALANCE_ADJUST)
    @PreAuthorize("@fortune.bookActorPermission(#adjustCommand.bookId)")
    public ResponseDTO<Void> balanceAdjust(@RequestBody @Valid FortuneAccountAdjustCommand adjustCommand) {
        fortuneAccountService.balanceAdjust(adjustCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "移入回收站")
    @PatchMapping("/{groupId}/{accountId}/moveToRecycleBin")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.MOVE_TO_RECYCLE_BIN)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.moveToRecycleBin(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账户")
    @DeleteMapping("/{groupId}/{accountId}/remove")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.ADD)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.remove(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "移出回收站")
    @PatchMapping("/{groupId}/{accountId}/putBack")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.PUT_BACK)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.putBack(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可支出")
    @PatchMapping("/{groupId}/{accountId}/canExpense")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_EXPENSE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> canExpense(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canExpense(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可支出")
    @PatchMapping("/{groupId}/{accountId}/cannotExpense")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_NOT_EXPENSE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> cannotExpense(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotExpense(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可收入")
    @PatchMapping("/{groupId}/{accountId}/canIncome")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_INCOME)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> canIncome(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canIncome(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可收入")
    @PatchMapping("/{groupId}/{accountId}/cannotIncome")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_NOT_INCOME)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> cannotIncome(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotIncome(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可转出")
    @PatchMapping("/{groupId}/{accountId}/canTransferOut")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_TRANSFER_OUT)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> canTransferOut(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canTransferOut(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可转出")
    @PatchMapping("/{groupId}/{accountId}/cannotTransferOut")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_NOT_TRANSFER_OUT)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> cannotTransferOut(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotTransferOut(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可转入")
    @PatchMapping("/{groupId}/{accountId}/canTransferIn")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_TRANSFER_IN)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> canTransferIn(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canTransferIn(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可转入")
    @PatchMapping("/{groupId}/{accountId}/cannotTransferIn")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.CAN_NOT_TRANSFER_IN)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> cannotTransferIn(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotTransferIn(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "计入净资产")
    @PatchMapping("/{groupId}/{accountId}/includeAccount")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.INCLUDE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> includeAccount(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.includeAccount(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不计入净资产")
    @PatchMapping("/{groupId}/{accountId}/excludeAccount")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.EXCLUDE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> excludeAccount(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.excludeAccount(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用")
    @PatchMapping("/{groupId}/{accountId}/enable")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.ENABLE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> enable(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.enable(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "停用")
    @PatchMapping("/{groupId}/{accountId}/disable")
    @AccessLog(title = "好记-账户管理", businessType = BusinessTypeEnum.DISABLE)
    @PreAuthorize("@fortune.groupActorPermission(#groupId)")
    public ResponseDTO<Void> disable(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.disable(groupId,accountId);
        return ResponseDTO.ok();
    }
}
