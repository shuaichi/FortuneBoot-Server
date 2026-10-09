package com.fortuneboot.service.fortune.order;

import com.fortuneboot.orderapi.FinanceOrderStage04TestApplication;
import com.fortuneboot.service.fortune.importer.FortuneBillImportResponse;
import com.fortuneboot.service.fortune.importer.FortuneBillImportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("账单导入与单据7/8整批事务")
@SpringBootTest(classes = FinanceOrderStage04TestApplication.class)
class FinanceOrderImportTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage04-import-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();
    private static final String[] HEADERS = {
            "标题（必填，50字以内）",
            "交易时间（必填，yyyy-MM-dd HH:mm:ss）",
            "流水类型（必填，支出/收入/转账/垫付/报销）",
            "账户（转账必填，其他可空）",
            "转入账户（转账必填）",
            "单据ID（垫付/报销必填）",
            "交易对象（选填，填名称）",
            "分类金额（支出/收入/垫付/报销必填，分类:金额；分类:金额）",
            "金额（转账必填，其他可空）",
            "标签（选填，标签1；标签2）",
            "成员（选填，成员1；成员2）",
            "是否确认（选填，是/否）",
            "是否统计（选填，是/否）",
            "备注（选填，512字以内）",
            "附加费用（选填，仅支出/转账，类型:金额:账户方向:分类:备注）",
            "附件（暂不支持，请留空）"
    };

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FortuneBillImportService importService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FinanceOrderStage03TestSupport.Stage03Fixture fixture;

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderStage03TestSupport.cleanAllTables(jdbcTemplate);
        this.fixture = FinanceOrderStage03TestSupport.insertFixture(jdbcTemplate);
        jdbcTemplate.update("""
                insert into fortune_category(category_id, category_type, category_name, book_id, parent_id,
                    enable, deleted)
                values (2, 2, '收入分类', ?, 0, 1, 0)
                """, fixture.bookIdA());
        insertReadyClaimOrder(1L);
    }

    @Test
    @DisplayName("7/8导入走统一管线：前3行成功第4行超报销时整批回滚")
    void orderImport_rollsBackWholeBatchWhenLastReimburseOversettles() throws IOException {
        byte[] excel = excel(
                row("垫付1", "2026-10-01 09:00:00", "垫付", "账户A", null, "1", null, "阶段03分类:100.00", "100.00"),
                row("垫付2", "2026-10-01 10:00:00", "垫付", "账户A", null, "1", null, "阶段03分类:200.00", "200.00"),
                row("报销1", "2026-10-02 09:00:00", "报销", "账户A", null, "1", null, "收入分类:50.00", "50.00"),
                row("报销超限", "2026-10-02 10:00:00", "报销", "账户A", null, "1", null, "收入分类:300.00", "300.00"));

        FortuneBillImportResponse response = importService.importByExcel(fixture.bookIdA(), file(excel));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorFile()).isNotEmpty();
        assertThat(billCount()).isZero();
        assertThat(accountBalance(fixture.accountIdA())).isEqualByComparingTo("10000.00");
        assertThat(orderAmount("out_amount")).isEqualByComparingTo("0.0000");
        assertThat(orderAmount("in_amount")).isEqualByComparingTo("0.0000");
        assertThat(orderVersion()).isZero();
        assertThat(orderAuditCount()).isZero();
        assertThat(orderRequestCount()).isZero();
    }

    @Test
    @DisplayName("7/8导入成功时更新单据缓存、余额并生成审计")
    void orderImport_usesUnifiedLedgerPipelineOnSuccess() throws IOException {
        byte[] excel = excel(
                row("垫付", "2026-10-01 09:00:00", "垫付", "账户A", null, "1", null, "阶段03分类:100.00", "100.00"),
                row("报销", "2026-10-02 09:00:00", "报销", "账户A", null, "1", null, "收入分类:40.00", "40.00"));

        FortuneBillImportResponse response = importService.importByExcel(fixture.bookIdA(), file(excel));

        assertThat(response.isSuccess()).as(errorText(response)).isTrue();
        assertThat(billCount()).isEqualTo(2);
        assertThat(accountBalance(fixture.accountIdA())).isEqualByComparingTo("9940.00");
        assertThat(orderAmount("out_amount")).isEqualByComparingTo("100.0000");
        assertThat(orderAmount("in_amount")).isEqualByComparingTo("40.0000");
        assertThat(orderVersion()).isEqualTo(2);
        assertThat(orderStatus()).isEqualTo(200);
        assertThat(orderAuditCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("普通1/2/3导入保持原路径不回归")
    void normalBillImport_remainsCompatible() throws IOException {
        byte[] excel = excel(
                row("普通支出", "2026-10-01 09:00:00", "支出", "账户A", null, null, null, "阶段03分类:100.00", "100.00"),
                row("普通收入", "2026-10-01 10:00:00", "收入", "账户A", null, null, null, "收入分类:50.00", "50.00"),
                row("普通转账", "2026-10-01 11:00:00", "转账", "账户A", "账户B", null, null, null, "30.00"));

        FortuneBillImportResponse response = importService.importByExcel(fixture.bookIdA(), file(excel));

        assertThat(response.isSuccess()).as(errorText(response)).isTrue();
        assertThat(billCount()).isEqualTo(3);
        assertThat(accountBalance(fixture.accountIdA())).isEqualByComparingTo("9920.00");
        assertThat(accountBalance(fixture.accountIdB())).isEqualByComparingTo("5030.00");
        assertThat(orderVersion()).isZero();
        assertThat(orderAuditCount()).isZero();
    }

    @Test
    @DisplayName("9—12导入明确拒绝且不落库")
    void loanImportTypes_areRejected() throws IOException {
        byte[] excel = excel(row("借出", "2026-10-01 09:00:00", "借出", "账户A", null, "1", null, null, "100.00"));

        FortuneBillImportResponse response = importService.importByExcel(fixture.bookIdA(), file(excel));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorFile()).isNotEmpty();
        assertThat(billCount()).isZero();
        assertThat(accountBalance(fixture.accountIdA())).isEqualByComparingTo("10000.00");
    }

    private void insertReadyClaimOrder(Long orderId) {
        jdbcTemplate.update("""
                insert into fortune_finance_order(order_id,book_id,title,type,status,submit_time,currency_code,
                    version,reconciliation_status,adjusted_amount,deleted)
                values (?,?,'导入报销单',1,100,'2026-10-01 08:00:00','CNY',0,'READY',0,0)
                """, orderId, fixture.bookIdA());
    }

    private MockMultipartFile file(byte[] content) {
        return new MockMultipartFile("file", "import.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
    }

    private byte[] excel(String[]... rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("账单导入");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < rows[rowIndex].length; columnIndex++) {
                    row.createCell(columnIndex).setCellValue(rows[rowIndex][columnIndex]);
                }
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String[] row(String title, String tradeTime, String billType, String account, String toAccount,
                         String orderId, String payee, String categories, String amount) {
        return new String[]{title, tradeTime, billType, account, toAccount, orderId, payee, categories, amount};
    }

    private String errorText(FortuneBillImportResponse response) {
        if (response.getErrorFile() == null || response.getErrorFile().length == 0) {
            return "导入失败，错误文件为空";
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(response.getErrorFile()))) {
            var sheet = workbook.getSheetAt(0);
            var formatter = new org.apache.poi.ss.usermodel.DataFormatter();
            StringBuilder builder = new StringBuilder();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                var row = sheet.getRow(rowIndex);
                if (row != null && row.getLastCellNum() > 0) {
                    builder.append(formatter.formatCellValue(row.getCell(row.getLastCellNum() - 1))).append(';');
                }
            }
            return builder.toString();
        } catch (IOException exception) {
            return "读取错误文件失败: " + exception.getMessage();
        }
    }

    private int billCount() {
        return jdbcTemplate.queryForObject("select count(*) from fortune_bill", Integer.class);
    }

    private BigDecimal accountBalance(Long accountId) {
        return jdbcTemplate.queryForObject(
                "select balance from fortune_account where account_id = ?", BigDecimal.class, accountId);
    }

    private BigDecimal orderAmount(String column) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from fortune_finance_order where order_id = 1", BigDecimal.class);
    }

    private Long orderVersion() {
        return jdbcTemplate.queryForObject(
                "select version from fortune_finance_order where order_id = 1", Long.class);
    }

    private Integer orderStatus() {
        return jdbcTemplate.queryForObject(
                "select status from fortune_finance_order where order_id = 1", Integer.class);
    }

    private int orderAuditCount() {
        return jdbcTemplate.queryForObject(
                "select count(*) from fortune_finance_order_audit where order_id = 1", Integer.class);
    }

    private int orderRequestCount() {
        return jdbcTemplate.queryForObject(
                "select count(*) from fortune_finance_order_request where book_id = ?", Integer.class, fixture.bookIdA());
    }
}
