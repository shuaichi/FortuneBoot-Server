package com.fortuneboot.controller.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.domain.command.fortune.FortuneBookAddCommand;
import com.fortuneboot.service.fortune.FortuneBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账本Controller
 *
 * @author zhangchi118
 * @date 2024/11/29 15:25
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/fortune/book/")
@Tag(name = "账本API", description = "账本相关的增删查改")
public class FortuneBookController {

    private final FortuneBookService fortuneBookService;

    @Operation(summary = "新增账本")
    @PostMapping("/add")
    public ResponseDTO<Void> addFortuneBook(@Valid @RequestBody FortuneBookAddCommand bookAddCommand) {
        fortuneBookService.addFortuneBook(bookAddCommand);
        return ResponseDTO.ok();
    }
}
