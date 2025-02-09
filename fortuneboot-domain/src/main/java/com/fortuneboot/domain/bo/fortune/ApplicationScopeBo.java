package com.fortuneboot.domain.bo.fortune;

import com.fortuneboot.domain.bo.fortune.tenplate.BookTemplateBo;
import com.fortuneboot.domain.bo.fortune.tenplate.CurrencyTemplateBo;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.List;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/17 00:18
 **/
@Data
@Component
@ApplicationScope
public class ApplicationScopeBo {

    /**
     * 账本模板
     */
    private List<BookTemplateBo> bookTemplateBoList;

    /**
     * 货币模板
     */
    private List<CurrencyTemplateBo> currencyTemplateBoList;
}
