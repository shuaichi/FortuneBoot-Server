package com.fortuneboot.controller.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.FortuneBookAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBookModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBookEntity;
import com.fortuneboot.domain.query.fortune.FortuneBookQuery;
import com.fortuneboot.domain.vo.fortune.FortuneBookVo;
import com.fortuneboot.service.fortune.FortuneBookService;
import com.fortuneboot.service.fortune.FortuneGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 账本Controller
 *
 * @author zhangchi118
 * @date 2024/11/29 15:25
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/book/base")
@Tag(name = "账本API", description = "账本相关的增删查改")
public class FortuneBookController {

    private final FortuneBookService fortuneBookService;

    private final FortuneGroupService fortuneGroupService;

    @Operation(summary = "分页查询账本")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.groupOwnerPermission(#query.getGroupId())")
    public ResponseDTO<PageDTO<FortuneBookVo>> getPage(@Valid FortuneBookQuery query) {
        IPage<FortuneBookEntity> page = fortuneBookService.getPage(query);
        List<FortuneBookVo> records = page.getRecords().stream().map(FortuneBookVo::new).collect(Collectors.toList());
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "新增账本")
    @PostMapping("/add")
    public ResponseDTO<Void> add(@Valid @RequestBody FortuneBookAddCommand bookAddCommand) {
        fortuneBookService.add(bookAddCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改账本")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.groupOwnerPermission(#bookModifyCommand.getGroupId())")
    public ResponseDTO<Void> modify(@Valid @RequestBody FortuneBookModifyCommand bookModifyCommand) {
        fortuneBookService.modify(bookModifyCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除账本")
    @DeleteMapping("/remove/{groupId}/{bookId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> removeFortuneBook(@PathVariable @Positive Long groupId, @PathVariable @Positive Long bookId) {
        fortuneBookService.remove(groupId, bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "移到回收站")
    @PatchMapping("/moveToRecycleBin/{groupId}/{bookId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long groupId, @PathVariable @Positive Long bookId) {
        fortuneBookService.moveToRecycleBin(groupId, bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "放回原处")
    @PatchMapping("/putBack/{groupId}/{bookId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long groupId, @PathVariable @Positive Long bookId) {
        fortuneBookService.putBack(groupId, bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "设置为默认账本")
    @PatchMapping("/setDefaultBook/{groupId}/{bookId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<Void> setDefaultBook(@PathVariable @Positive Long groupId, @PathVariable @Positive Long bookId) {
        fortuneGroupService.setDefaultBook(groupId, bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "设置为默认账本")
    @GetMapping("/getByGroupId/{groupId}")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<List<FortuneBookVo>> getByGroupId(@PathVariable @Positive Long groupId){
        List<FortuneBookEntity> bookEntityList = fortuneBookService.getByGroupId(groupId);
        List<FortuneBookVo> result = bookEntityList.stream().map(FortuneBookVo::new).collect(Collectors.toList());
        return ResponseDTO.ok(result);
    }
    // TODO 导出账单、复制账本
}
