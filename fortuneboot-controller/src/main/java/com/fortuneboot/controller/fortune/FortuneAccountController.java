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
    public ResponseDTO<PageDTO<FortuneAccountVo>> getPage(@Valid @RequestBody FortuneAccountQuery query) {
        IPage<FortuneAccountEntity> page = fortuneAccountService.getPage(query);
        List<FortuneAccountVo> records = page.getRecords().stream().map(FortuneAccountVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "新增账户")
    @PostMapping("/add")
    @PreAuthorize("@fortune.groupOwnerPermission(#addCommand.getGroupId())")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneAccountAddCommand addCommand) {
        fortuneAccountService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改账户")
    @PostMapping("/modify")
    @PreAuthorize("@fortune.groupOwnerPermission(#modifyCommand.getGroupId())")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneAccountModifyCommand modifyCommand) {
        fortuneAccountService.modify(modifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "移入回收站")
    @PatchMapping("/moveToRecycleBin/{groupId}/{accountId}}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.moveToRecycleBin(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账户")
    @DeleteMapping("/remove/{groupId}/{accountId}}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> remove(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.remove(groupId,accountId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账户")
    @PatchMapping("/putBack/{groupId}/{accountId}}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long groupId, @PathVariable @Positive Long accountId){
        fortuneAccountService.putBack(groupId,accountId);
        return ResponseDTO.ok();
    }
}
