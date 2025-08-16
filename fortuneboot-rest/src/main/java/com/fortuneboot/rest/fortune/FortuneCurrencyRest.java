package com.fortuneboot.rest.fortune;

import com.fortuneboot.common.core.dto.ResponseDTO;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import com.fortuneboot.domain.query.fortune.FortuneCurrencyQuery;
import com.fortuneboot.domain.vo.fortune.FortuneCurrencyVo;
import com.fortuneboot.service.fortune.FortuneCurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 汇率接口
 *
 * @author zhangchi118
 * @date 2025/3/29 17:40
 **/
@RestController
@RequestMapping("/fortune/currency")
@AllArgsConstructor
public class FortuneCurrencyRest {

    private final FortuneCurrencyService fortuneCurrencyService;

    @Operation(summary = "刷新汇率")
    @PostMapping("/refresh")
    public ResponseDTO<Void> refresh() {
        fortuneCurrencyService.refreshCurrency();
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询汇率")
    @GetMapping
    public ResponseDTO<List<FortuneCurrencyVo>> get(FortuneCurrencyQuery query) {
        List<CurrencyTemplateBo> currencyTemplateBoList = fortuneCurrencyService.getCurrencyTemplateBoList(query);
        List<FortuneCurrencyVo> result = currencyTemplateBoList.stream().map(item -> new FortuneCurrencyVo(item, query.getBase())).toList();
        return ResponseDTO.ok(result);
    }

}
