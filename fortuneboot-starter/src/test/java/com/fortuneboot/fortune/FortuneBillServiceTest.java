package com.fortuneboot.fortune;

import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.service.fortune.FortuneBillService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author zhangchi118
 * @date 2025/2/28 18:19
 **/
@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@ExtendWith(SpringExtension.class)
public class FortuneBillServiceTest {

    @Resource
    private FortuneBillService fortuneBillService;

    @Test
    public void testGetCategoryExpense(){
        CategoryIncludeQuery query = new CategoryIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getCategoryExpense(query);
        log.info("[testGetCategoryExpense] result = {}", result);
    }

    @Test
    public void testGetCategoryIncome(){
        CategoryIncludeQuery query = new CategoryIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getCategoryIncome(query);
        log.info("[testGetCategoryIncome] result = {}", result);
    }

    @Test
    public void testGetTagExpense(){
        TagIncludeQuery query = new TagIncludeQuery();
        query.setBookId(1L);
        List<FortuneBarVo> result = fortuneBillService.getTagExpense(query);
        log.info("[testGetTagExpense] result = {}", result);
    }

    @Test
    public void testGetTagIncome(){
        TagIncludeQuery query = new TagIncludeQuery();
        query.setBookId(1L);
        List<FortuneBarVo> result = fortuneBillService.getTagIncome(query);
        log.info("[testGetTagIncome] result = {}", result);
    }

    @Test
    public void testGetPayeeExpense(){
        PayeeIncludeQuery query = new PayeeIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getPayeeExpense(query);
        log.info("[testGetPayeeExpense] result = {}", result);
    }

    @Test
    public void testGetPayeeIncome(){
        PayeeIncludeQuery query = new PayeeIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getPayeeIncome(query);
        log.info("[testGetPayeeIncome] result = {}", result);
    }
}
