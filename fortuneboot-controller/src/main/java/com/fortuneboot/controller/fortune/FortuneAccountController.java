package com.fortuneboot.controller.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.FortuneAccountAddCommand;
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
    @PreAuthorize("@fortune.groupOwnerPermission(#query.getGroupId())")
    public ResponseDTO<PageDTO<FortuneAccountVo>> getPage(@Valid FortuneAccountQuery query) {
        IPage<FortuneAccountEntity> page = fortuneAccountService.getPage(query);
        List<FortuneAccountVo> records = page.getRecords().stream().map(FortuneAccountVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "查询启用的账户")
    @GetMapping("/{groupId}/getEnableList")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<List<FortuneAccountVo>> getEnableAccountList(@PathVariable @NotNull @Positive Long groupId){
        List<FortuneAccountEntity> list = fortuneAccountService.getEnableAccountList(groupId);
        List<FortuneAccountVo> result = list.stream().map(FortuneAccountVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增账户")
    @PostMapping("/add")
    @PreAuthorize("@fortune.groupOwnerPermission(#addCommand.getGroupId())")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneAccountAddCommand addCommand) {
        fortuneAccountService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改账户")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.groupOwnerPermission(#modifyCommand.getGroupId())")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneAccountModifyCommand modifyCommand) {
        fortuneAccountService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "移入回收站")
    @PatchMapping("/{groupId}/{accountId}/moveToRecycleBin")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.moveToRecycleBin(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账户")
    @DeleteMapping("/{groupId}/{accountId}/remove")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.remove(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账户")
    @PatchMapping("/{groupId}/{accountId}/putBack")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.putBack(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可支出")
    @PatchMapping("/{groupId}/{accountId}/canExpense")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> canExpense(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canExpense(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可支出")
    @PatchMapping("/{groupId}/{accountId}/cannotExpense")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> cannotExpense(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotExpense(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可收入")
    @PatchMapping("/{groupId}/{accountId}/canIncome")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> canIncome(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canIncome(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可收入")
    @PatchMapping("/{groupId}/{accountId}/cannotIncome")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> cannotIncome(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotIncome(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可转出")
    @PatchMapping("/{groupId}/{accountId}/canTransferOut")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> canTransferOut(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canTransferOut(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可转出")
    @PatchMapping("/{groupId}/{accountId}/cannotTransferOut")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> cannotTransferOut(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotTransferOut(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "可转入")
    @PatchMapping("/{groupId}/{accountId}/canTransferIn")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> canTransferIn(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.canTransferIn(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不可转入")
    @PatchMapping("/{groupId}/{accountId}/cannotTransferIn")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> cannotTransferIn(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.cannotTransferIn(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "计入净资产")
    @PatchMapping("/{groupId}/{accountId}/includeAccount")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> includeAccount(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.includeAccount(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "不计入净资产")
    @PatchMapping("/{groupId}/{accountId}/excludeAccount")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> excludeAccount(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.excludeAccount(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "启用")
    @PatchMapping("/{groupId}/{accountId}/enable")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> enable(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.enable(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "停用")
    @PatchMapping("/{groupId}/{accountId}/disable")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> disable(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.disable(groupId,accountId);
        return ResponseDTO.ok();
    }
}
