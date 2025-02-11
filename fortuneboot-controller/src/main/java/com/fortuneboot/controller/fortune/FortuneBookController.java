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
import jakarta.validation.constraints.NotNull;
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
        List<FortuneBookVo> records = page.getRecords().stream().map(FortuneBookVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "查询启用的账本")
    @GetMapping("/{groupId}/getEnableList")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<List<FortuneBookVo>> getEnableBookList(@PathVariable @NotNull @Positive Long groupId) {
        List<FortuneBookEntity> list = fortuneBookService.getEnableBookList(groupId);
        List<FortuneBookVo> result = list.stream().map(FortuneBookVo::new).toList();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增账本")
    @PostMapping("/add")
    @PreAuthorize("@fortune.groupOwnerPermission(#bookAddCommand.getGroupId())")
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
    @DeleteMapping("/{bookId}/remove")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> removeFortuneBook(@PathVariable @Positive Long bookId) {
        fortuneBookService.remove(bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "移到回收站")
    @PatchMapping("/{bookId}/moveToRecycleBin")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> moveToRecycleBin(@PathVariable @Positive Long bookId) {
        fortuneBookService.moveToRecycleBin(bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "放回原处")
    @PatchMapping("/{bookId}/putBack")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> putBack(@PathVariable @Positive Long bookId) {
        fortuneBookService.putBack(bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "设置为默认账本")
    @PatchMapping("/{bookId}/setDefaultBook")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> setDefaultBook(@PathVariable @Positive Long bookId) {
        fortuneGroupService.setDefaultBook(bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "通过分组id查询")
    @GetMapping("/{groupId}/getByGroupId")
    @PreAuthorize("@fortune.groupOwnerPermission(#groupId)")
    public ResponseDTO<List<FortuneBookVo>> getByGroupId(@PathVariable @Positive Long groupId) {
        List<FortuneBookEntity> bookEntityList = fortuneBookService.getByGroupId(groupId);
        List<FortuneBookVo> result = bookEntityList.stream().map(FortuneBookVo::new).collect(Collectors.toList());
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "启用账本")
    @PatchMapping("/{bookId}/enable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> enable(@PathVariable @Positive Long bookId) {
        fortuneBookService.enable(bookId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "停用账本")
    @PatchMapping("/{bookId}/disable")
    @PreAuthorize("@fortune.bookOwnerPermission(#bookId)")
    public ResponseDTO<Void> disable(@PathVariable @NotNull @Positive Long bookId) {
        fortuneBookService.disable(bookId);
        return ResponseDTO.ok();
    }
    // TODO 导出账单、复制账本
}
