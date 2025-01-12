package com.fortuneboot.controller.fortune;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.FortuneBillAddCommand;
import com.fortuneboot.domain.command.fortune.FortuneBillModifyCommand;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.FortuneBillVo;
import com.fortuneboot.service.fortune.FortuneBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2025/1/12 22:39
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/balance/flow")
@Tag(name = "账单API", description = "账单相关的增删查改")
public class FortuneBillController {

    private final FortuneBillService fortuneBillService;

    @Operation(summary = "分页查询账户")
    @GetMapping("/getPage")
    @PreAuthorize("@fortune.bookOwnerPermission(#query.getBookId())")
    public ResponseDTO<PageDTO<FortuneBillVo>> getPage(@Valid @RequestBody FortuneBillQuery query){
        IPage<FortuneBillEntity> page = fortuneBillService.getPage(query);
        List<FortuneBillVo> records = page.getRecords().stream().map(FortuneBillVo::new).toList();
        return ResponseDTO.ok(new PageDTO<>(records, page.getTotal()));
    }

    @Operation(summary = "分页查询账户")
    @PostMapping("/add")
    @PreAuthorize("@fortune.bookOwnerPermission(#addCommand.getBookId())")
    public ResponseDTO<Void> add(FortuneBillAddCommand addCommand){
        fortuneBillService.add(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "分页查询账户")
    @PutMapping("/modify")
    @PreAuthorize("@fortune.bookOwnerPermission(#modifyCommand.getBookId())")
    public ResponseDTO<Void> add(FortuneBillModifyCommand modifyCommand){
        fortuneBillService.modify(modifyCommand);
        return ResponseDTO.ok();
    }
}
