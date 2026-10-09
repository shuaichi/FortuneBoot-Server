package com.fortuneboot.service.fortune.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.common.core.page.PageDTO;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderTransitionCommand;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.query.fortune.FortuneBillQuery;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageQuery;
import com.fortuneboot.domain.vo.fortune.include.BillStatisticsVo;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitVo;
import com.fortuneboot.domain.vo.fortune.include.FinanceProfitQuery;
import com.fortuneboot.domain.vo.fortune.include.LoanOverviewVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderDetailVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderSummaryVo;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import com.fortuneboot.orderapi.FinanceOrderStage04TestApplication;
import com.fortuneboot.service.fortune.FortuneBillService;
import com.fortuneboot.service.fortune.FortuneIncludeService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("单据统计、旧借贷总览与报告导出")
@SpringBootTest(classes = FinanceOrderStage04TestApplication.class)
class FinanceOrderReportTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage04-report-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FinanceOrderRequestService requestService;
    @Autowired
    private FinanceOrderQueryService queryService;
    @Autowired
    private FortuneIncludeService includeService;
    @Autowired
    private FortuneBillService billService;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private MockMvc mockMvc;

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
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        login(fixture.ownerUserId());
    }

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("同对象两单保持两行，旧汇总按对象聚合且不同账本同名不合并")
    void loanOverview_groupsByObjectWhileV2CountsOrders() {
        Long first = createOrder("同对象债权1", 2, 1L, "张三", fixture.bookIdA());
        addPrincipal(first, 9, "100.00");
        Long second = createOrder("同对象债权2", 2, 1L, "张三", fixture.bookIdA());
        addPrincipal(second, 9, "200.00");
        Long otherBook = createOrder("账本B同名债权", 2, null, "张三", fixture.bookIdB());
        addPrincipal(otherBook, 9, "50.00", fixture.bookIdB());

        FinanceOrderPageQuery pageQuery = new FinanceOrderPageQuery();
        pageQuery.setBookId(fixture.bookIdA());
        pageQuery.setType(2);
        PageDTO<FinanceOrderDetailVo> page = queryService.page(pageQuery);
        FinanceOrderSummaryVo summary = queryService.summary(fixture.bookIdA());
        LoanOverviewVo overviewA = includeService.getLoanOverview(fixture.bookIdA());
        LoanOverviewVo overviewB = includeService.getLoanOverview(fixture.bookIdB());

        assertThat(page.getRows()).hasSize(2);
        assertThat(summary.getReceivableCount()).isEqualTo(2);
        assertThat(new BigDecimal(summary.getReceivableAmount())).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(overviewA.getReceivableCount()).isEqualTo(1);
        assertThat(overviewA.getTotalReceivable()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(overviewB.getReceivableCount()).isEqualTo(1);
        assertThat(overviewB.getTotalReceivable()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("费用报销单不进入借贷债权，本金include切换不改变待收")
    void claimAndIncludeDoNotChangeLoanAggregation() {
        Long claim = createOrder("报销单", 1, null, "单位", fixture.bookIdA());
        addPrincipal(claim, 7, "100.00");
        Long loan = createOrder("债权", 2, 1L, "张三", fixture.bookIdA());
        FinanceOrderMutationVo bill = addPrincipal(loan, 9, "100.00");
        requestService.legacyInclude(fixture.bookIdA(), bill.getBillId(), false);

        FinanceOrderSummaryVo summary = queryService.summary(fixture.bookIdA());
        LoanOverviewVo overview = includeService.getLoanOverview(fixture.bookIdA());

        assertThat(summary.getReceivableCount()).isEqualTo(1);
        assertThat(summary.getReimbursementCount()).isEqualTo(1);
        assertThat(new BigDecimal(summary.getReceivableAmount())).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(overview.getReceivableCount()).isEqualTo(1);
        assertThat(overview.getTotalReceivable()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(new BigDecimal(queryService.detail(fixture.bookIdA(), loan).getPendingAmount()))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("普通统计只认确认未作废的关联费用/自付，归档借款收益使用net/issued")
    void statisticsUseConfirmedEntriesAndArchivedLoanNetProfit() {
        Long loan = createOrder("统计债权", 2, 1L, "张三", fixture.bookIdA());
        FinanceOrderMutationVo pendingFee = addComponent(loan, "FEE", 1, "5.00", false);
        assertThat(statistics().getExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(loan)
                .billId(pendingFee.getBillId())
                .operation(FinanceOrderEntryMutation.Operation.VOID)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(pendingFee.getVersion())
                .build());
        FinanceOrderMutationVo issued = addPrincipal(loan, 9, "100.00");

        addComponent(loan, "FEE", 1, "5.00", true);
        Long claim = createOrder("统计报销", 1, null, "单位", fixture.bookIdA());
        addPrincipal(claim, 7, "100.00");
        FinanceOrderMutationVo selfPay = addComponent(claim, "WRITE_OFF", 1, "20.00", true);
        FinanceOrderMutationVo voidedSelfPay = addComponent(claim, "WRITE_OFF", 1, "30.00", true);
        requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(claim)
                .billId(voidedSelfPay.getBillId())
                .operation(FinanceOrderEntryMutation.Operation.VOID)
                .requestId(UUID.randomUUID().toString())
                .expectedVersion(voidedSelfPay.getVersion())
                .build());

        BillStatisticsVo statistics = statistics();
        assertThat(statistics.getExpense()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(selfPay.getBillId()).isNotNull();

        addPrincipal(loan, 10, "100.00");
        addComponent(loan, "INTEREST", 2, "20.00", true);
        close(loan, issued.getVersion());

        List<FinanceProfitVo> profits = includeService.getFinanceProfit(fixture.bookIdA(), new FinanceProfitQuery());
        assertThat(profits).hasSize(1);
        assertThat(profits.get(0).getProfit()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(profits.get(0).getOutAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(profits.get(0).getProfitRate()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    @DisplayName("无权限用户不能导出单据核对报告")
    void unauthorizedUserCannotExportReconciliation() throws Exception {
        login(fixture.outsiderUserId());
        assertThatThrownBy(() -> mockMvc.perform(get("/fortune/finance/order/v2/{bookId}/reconciliation/export",
                        fixture.bookIdA())
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()))
                .hasRootCauseInstanceOf(org.springframework.security.authorization.AuthorizationDeniedException.class);
    }

    private BillStatisticsVo statistics() {
        FortuneBillQuery query = new FortuneBillQuery();
        query.setBookId(fixture.bookIdA());
        query.setInclude(true);
        return billService.getBillStatistics(query);
    }

    private Long createOrder(String title, Integer type, Long counterpartyId, String counterpartyName, Long bookId) {
        FinanceOrderCreateCommand command = new FinanceOrderCreateCommand();
        command.setBookId(bookId);
        command.setRequestId(UUID.randomUUID().toString());
        command.setTitle(title);
        command.setType(type);
        command.setCounterpartyId(counterpartyId);
        command.setCounterpartyName(counterpartyName);
        command.setRemark("");
        return requestService.create(command).getOrderId();
    }

    private FinanceOrderMutationVo addPrincipal(Long orderId, Integer billType, String amount) {
        return addPrincipal(orderId, billType, amount, fixture.bookIdA());
    }

    private FinanceOrderMutationVo addPrincipal(Long orderId, Integer billType, String amount, Long bookId) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(bookId);
        command.setOrderId(orderId);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(queryService.detail(bookId, orderId).getVersion());
        command.setTitle("流水");
        command.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        command.setBillType(billType);
        command.setOrderComponent("PRINCIPAL");
        if (billType == 7 || billType == 8) {
            command.setAmount(null);
            command.setCategoryAmountPair(List.of(new CategoryAmountDTO(
                    billType == 7 ? 1L : 2L, new BigDecimal(amount))));
        } else {
            command.setAmount(new BigDecimal(amount));
            command.setCategoryAmountPair(List.of());
        }
        command.setAccountId(fixture.accountIdA());
        command.setConfirm(true);
        command.setInclude(false);
        return requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(bookId)
                .orderId(orderId)
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(command)
                .requestId(command.getRequestId())
                .expectedVersion(command.getExpectedVersion())
                .fileList(List.of())
                .build());
    }

    private FinanceOrderMutationVo addComponent(Long orderId, String component, Integer billType, String amount,
                                                 boolean confirm) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(fixture.bookIdA());
        command.setOrderId(orderId);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(queryService.detail(fixture.bookIdA(), orderId).getVersion());
        command.setTitle(component);
        command.setTradeTime(LocalDateTime.of(2026, 10, 2, 9, 0));
        command.setBillType(billType);
        command.setOrderComponent(component);
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(
                billType == 1 ? 1L : 2L, new BigDecimal(amount))));
        command.setAccountId(component.equals("WRITE_OFF") ? null : fixture.accountIdA());
        command.setConfirm(confirm);
        command.setInclude(true);
        command.setRemark(component.equals("WRITE_OFF") ? "原因" : "");
        return requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdA())
                .orderId(orderId)
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(command)
                .requestId(command.getRequestId())
                .expectedVersion(command.getExpectedVersion())
                .fileList(List.of())
                .build());
    }

    private void close(Long orderId, Long expectedVersion) {
        FinanceOrderTransitionCommand command = new FinanceOrderTransitionCommand();
        command.setBookId(fixture.bookIdA());
        command.setOrderId(orderId);
        command.setRequestId(UUID.randomUUID().toString());
        command.setExpectedVersion(queryService.detail(fixture.bookIdA(), orderId).getVersion());
        command.setAction(FinanceOrderTransitionCommand.Action.CLOSE);
        requestService.transition(command);
    }

    private void login(Long userId) {
        SystemLoginUser loginUser = new SystemLoginUser(userId, false, "user" + userId, "", null);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
