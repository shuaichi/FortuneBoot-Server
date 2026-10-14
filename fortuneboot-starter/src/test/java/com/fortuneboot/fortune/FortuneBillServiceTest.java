package com.fortuneboot.fortune;

import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.bo.fortune.FortuneBillBo;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.bill.FortuneBillImportExcelVo;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.vo.fortune.include.*;
import com.fortuneboot.integrationTest.IntegrationTestApplication;
import com.fortuneboot.service.fortune.FortuneBillService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author zhangchi118
 * @date 2025/2/28 18:19
 **/
@Slf4j
@SpringBootTest(classes = IntegrationTestApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"basic", "test"})
public class FortuneBillServiceTest {

    /**
     * Spring 测试构造器自动装配默认关闭，这里遵循仓库集成测试惯例使用字段注入
     */
    @Resource
    private FortuneBillService fortuneBillService;

    @Test
    public void testListImportExcelRows() {
        FortuneBillQuery query = new FortuneBillQuery();
        query.setBookId(1L);
        List<FortuneBillImportExcelVo> rows = fortuneBillService.listImportExcelRows(query);
        log.info("[testListImportExcelRows] rows = {}", rows);
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.getBillType()).isNotBlank();
            if (row.getExtras() != null) {
                assertThat(row.getExtras())
                        .matches("^((手续费|优惠):[^:]+:(转出账户|转入账户):[^；]*)(；(手续费|优惠):[^:]+:(转出账户|转入账户):[^；]*)*$");
            }
        });
    }

    @Test
    public void testGetCategoryExpense() {
        CategoryIncludeQuery query = new CategoryIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getCategoryExpense(query);
        log.info("[testGetCategoryExpense] result = {}", result);
    }

    @Test
    public void testGetCategoryIncome() {
        CategoryIncludeQuery query = new CategoryIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getCategoryIncome(query);
        log.info("[testGetCategoryIncome] result = {}", result);
    }

    @Test
    public void testGetTagExpense() {
        TagIncludeQuery query = new TagIncludeQuery();
        query.setBookId(1L);
        List<FortuneBarVo> result = fortuneBillService.getTagExpense(query);
        log.info("[testGetTagExpense] result = {}", result);
    }

    @Test
    public void testGetTagIncome() {
        TagIncludeQuery query = new TagIncludeQuery();
        query.setBookId(1L);
        List<FortuneBarVo> result = fortuneBillService.getTagIncome(query);
        log.info("[testGetTagIncome] result = {}", result);
    }

    @Test
    public void testGetPayeeExpense() {
        PayeeIncludeQuery query = new PayeeIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getPayeeExpense(query);
        log.info("[testGetPayeeExpense] result = {}", result);
    }

    @Test
    public void testGetPayeeIncome() {
        PayeeIncludeQuery query = new PayeeIncludeQuery();
        query.setBookId(1L);
        List<FortunePieVo> result = fortuneBillService.getPayeeIncome(query);
        log.info("[testGetPayeeIncome] result = {}", result);
    }

    @Test
    public void testGetPage(){
        FortuneBillQuery fortuneBillQuery = new FortuneBillQuery();
        fortuneBillQuery.setOrderColumn("amount");
        fortuneBillQuery.setOrderDirection("ascending");
        PageDTO<FortuneBillBo> result = fortuneBillService.getPage(fortuneBillQuery);
        log.info("[testGetPage] result = {}", result.toString());
    }
}
