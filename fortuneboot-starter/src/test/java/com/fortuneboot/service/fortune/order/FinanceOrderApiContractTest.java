package com.fortuneboot.service.fortune.order;

import com.fortuneboot.orderapi.FinanceOrderStage03TestApplication;

import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import com.fortuneboot.common.utils.jackson.JacksonUtil;
import com.fortuneboot.infrastructure.user.web.SystemLoginUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("单据V2与旧HTTP契约")
@SpringBootTest(classes = FinanceOrderStage03TestApplication.class)
class FinanceOrderApiContractTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage03-http-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();
    private static final int SUCCESS = 0;

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @Autowired
    private FinanceOrderRequestService requestService;
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
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        login(fixture.ownerUserId());
    }

    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("旧表单add/modify与旧multipart bill保持兼容")
    void legacyFormAndMultipartEndpointsRemainCompatible() throws Exception {
        JsonNode add = read(mockMvc.perform(post("/fortune/finance/order/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("title", "旧接口单据")
                        .param("type", "2")
                        .param("remark", "旧备注"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(add.get("code").asInt()).isEqualTo(SUCCESS);
        assertThat(add.get("data").asBoolean()).isTrue();
        Long orderId = jdbcTemplate.queryForObject(
                "select order_id from fortune_finance_order where title = '旧接口单据'", Long.class);

        JsonNode modify = read(mockMvc.perform(put("/fortune/finance/order/modify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("orderId", String.valueOf(orderId))
                        .param("title", "旧接口修改后")
                        .param("type", "2")
                        .param("remark", "新备注"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(modify.get("code").asInt()).isEqualTo(SUCCESS);
        assertThat(modify.get("data").asBoolean()).isTrue();

        JsonNode crossBookModify = read(mockMvc.perform(put("/fortune/finance/order/modify")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("bookId", String.valueOf(fixture.bookIdB()))
                        .param("orderId", String.valueOf(orderId))
                        .param("title", "跨账本修改")
                        .param("type", "2"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(crossBookModify.get("code").asInt()).isEqualTo(30003);

        JsonNode bill = read(mockMvc.perform(multipart("/fortune/bill/add")
                        .file(dataPart(orderLinkedBillJson(orderId, 1L, 9, "1000.00", true))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(bill.get("code").asInt()).isEqualTo(SUCCESS);
        JsonNode detail = detail(orderId);
        assertThat(detail.get("data").get("pendingAmount").asText()).isEqualTo("1000.00");

        JsonNode invalidType = read(mockMvc.perform(post("/fortune/finance/order/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("title", "非法类型")
                        .param("type", "99"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(invalidType.get("code").asInt()).isEqualTo(30008);
    }

    @Test
    @DisplayName("V2 JSON create幂等返回同orderId")
    void v2CreateIsIdempotent() throws Exception {
        String requestId = UUID.randomUUID().toString();
        String body = createJson(requestId, "张三 · 周转借款", 2, "2026-10-07");
        JsonNode first = read(mockMvc.perform(post("/fortune/finance/order/v2/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(first.get("code").asInt()).isEqualTo(SUCCESS);
        assertThat(first.at("/data/orderId").asLong()).isPositive();
        assertThat(first.at("/data/version").asLong()).isZero();
        assertThat(first.at("/data/replayed").asBoolean()).isFalse();

        JsonNode replay = read(mockMvc.perform(post("/fortune/finance/order/v2/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(replay.at("/data/orderId").asLong()).isEqualTo(first.at("/data/orderId").asLong());
        assertThat(replay.at("/data/replayed").asBoolean()).isTrue();

        String draftModify = """
                {"bookId":%d,"requestId":"%s","orderId":%d,"expectedVersion":0,"title":"草稿零版本修改","type":2,
                "counterpartyName":"张三","dueDate":null,"remark":""}
                """.formatted(fixture.bookIdA(), UUID.randomUUID(),
                first.at("/data/orderId").asLong());
        JsonNode modified = read(mockMvc.perform(put("/fortune/finance/order/v2/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(draftModify))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(modified.get("code").asInt()).isEqualTo(SUCCESS);
        assertThat(modified.at("/data/version").asLong()).isEqualTo(1L);

        String conflict = createJson(requestId, "另一个标题", 2, null);
        JsonNode rejected = read(mockMvc.perform(post("/fortune/finance/order/v2/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflict))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(rejected.get("code").asInt()).isEqualTo(30016);
    }

    @Test
    @DisplayName("V2 multipart entry创建本金流水并返回详情动作")
    void v2EntryMultipartCreatesPrincipalEntry() throws Exception {
        Long orderId = createOrder("张三 · 周转借款", 2, "2026-10-07");
        String entryBody = entryJson(orderId, 0L, UUID.randomUUID().toString(), 9, "1000.00",
                fixture.accountIdA(), true, false);
        JsonNode entry = read(mockMvc.perform(multipart("/fortune/finance/order/v2/{bookId}/{orderId}/entries",
                        fixture.bookIdA(), orderId)
                        .file(dataPart(entryBody)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(entry.get("code").asInt()).isEqualTo(SUCCESS);
        assertThat(entry.at("/data/billId").asLong()).isPositive();
        assertThat(entry.at("/data/version").asLong()).isEqualTo(1L);

        JsonNode detail = detail(orderId);
        assertThat(detail.get("data").get("pendingAmount").asText()).isEqualTo("1000.00");
        assertThat(detail.get("data").get("status").asInt()).isEqualTo(200);
        assertThat(detail.get("data").get("allowedEntryActions").toString()).contains("LOAN_RECOVER");
    }

    @Test
    @DisplayName("错误content-type与路径/body冲突明确拒绝")
    void wrongContentTypeAndPathBodyConflictRejected() throws Exception {
        String wrongContentTypeBody = mockMvc.perform(post("/fortune/finance/order/v2/create")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("title", "表单")
                        .param("type", "2")
                        .param("counterpartyName", "张三"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(read(wrongContentTypeBody).get("code").asInt()).isNotEqualTo(SUCCESS);

        Long orderId = createOrder("路径冲突单据", 2, null);
        String entryBody = entryJson(orderId, 0L, UUID.randomUUID().toString(), 9, "1000.00",
                fixture.accountIdA(), true, false);
        JsonNode entryConflict = read(mockMvc.perform(multipart(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries", fixture.bookIdA(), orderId)
                        .file(dataPart(entryBody.replace("\"bookId\":" + fixture.bookIdA(),
                                "\"bookId\":" + fixture.bookIdB()))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(entryConflict.get("code").asInt()).isEqualTo(30003);

        JsonNode transitionConflict = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/close", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":0,"
                                + "\"bookId\":" + fixture.bookIdB() + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(transitionConflict.get("code").asInt()).isEqualTo(30003);
    }

    @Test
    @DisplayName("访客可读不可写且详情为只读能力")
    void visitorCanReadButNotWrite() throws Exception {
        Long orderId = createOrder("访客只读单据", 2, null);
        login(fixture.visitorUserId());

        JsonNode detail = detail(orderId);
        assertThat(detail.get("code").asInt()).isEqualTo(SUCCESS);
        assertThat(detail.get("data").get("canEdit").asBoolean()).isFalse();
        assertThat(detail.get("data").get("allowedEntryActions").isEmpty()).isTrue();

        JsonNode page = read(mockMvc.perform(get("/fortune/finance/order/v2/page")
                        .param("bookId", String.valueOf(fixture.bookIdA())))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(page.get("code").asInt()).isEqualTo(SUCCESS);

        JsonNode createDenied = read(mockMvc.perform(post("/fortune/finance/order/v2/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(UUID.randomUUID().toString(), "访客新建", 2, null)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(createDenied.get("code").asInt()).isEqualTo(10202);

        JsonNode transitionDenied = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/close", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":0}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(transitionDenied.get("code").asInt()).isEqualTo(10202);
    }

    @Test
    @DisplayName("可访问账本A但目标单据属于B/C时全部拒绝")
    void accessibleBookButForeignOrderRejectedEverywhere() throws Exception {
        FinanceOrderCreateCommand foreignCommand = createCommand("账本B单据", 2);
        foreignCommand.setBookId(fixture.bookIdB());
        FinanceOrderMutationVo foreign = requestService.create(foreignCommand);
        FinanceOrderEntryCommand entry = entryCommand(foreign.getOrderId(), 0L, 9, "1000.00",
                fixture.accountIdA(), true, UUID.randomUUID());
        FinanceOrderMutationVo entryResult = requestService.mutateEntry(FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookIdB())
                .orderId(foreign.getOrderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(entry)
                .requestId(entry.getRequestId())
                .expectedVersion(0L)
                .fileList(List.of())
                .build());

        JsonNode wrongDetail = read(mockMvc.perform(get(
                        "/fortune/finance/order/v2/{bookId}/{orderId}", fixture.bookIdA(), foreign.getOrderId()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(wrongDetail.get("code").asInt()).isEqualTo(30003);

        JsonNode wrongEntries = read(mockMvc.perform(get(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries",
                        fixture.bookIdA(), foreign.getOrderId()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(wrongEntries.get("code").asInt()).isEqualTo(30003);

        JsonNode wrongAudit = read(mockMvc.perform(get(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/audit", fixture.bookIdA(), foreign.getOrderId()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(wrongAudit.get("code").asInt()).isEqualTo(30003);

        JsonNode wrongRestore = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries/{billId}/restore",
                        fixture.bookIdA(), foreign.getOrderId(), entryResult.getBillId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":1}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(wrongRestore.get("code").asInt()).isEqualTo(30003);
    }

    @Test
    @DisplayName("空单删除与流水历史竞态：已作废和legacy deleted均阻止")
    void emptyDeleteRaceAndHistoryChecks() throws Exception {
        Long orderId = createOrder("空单据", 2, null);
        JsonNode removed = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/remove", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":0}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(removed.get("code").asInt()).isEqualTo(SUCCESS);

        JsonNode removedAgain = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/remove", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":1}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(removedAgain.get("code").asInt()).isNotEqualTo(SUCCESS);

        Long voidedOrder = createOrder("作废流水单据", 2, null);
        JsonNode added = read(mockMvc.perform(multipart(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries", fixture.bookIdA(), voidedOrder)
                        .file(dataPart(entryJson(voidedOrder, 0L, UUID.randomUUID().toString(), 9,
                                "1000.00", fixture.accountIdA(), true, false))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        Long billId = added.at("/data/billId").asLong();
        read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries/{billId}/void",
                        fixture.bookIdA(), voidedOrder, billId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":1}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        JsonNode removeVoided = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/remove", fixture.bookIdA(), voidedOrder)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":2}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(removeVoided.get("code").asInt()).isEqualTo(30018);

        Long legacyOrder = createOrder("历史删除单据", 2, null);
        jdbcTemplate.update("""
                insert into fortune_bill(book_id, title, trade_time, account_id, amount, converted_amount, bill_type,
                    confirm, include, order_id, order_component, order_ledger_version, recycle_bin, deleted)
                values (?, '历史流水', '2026-10-01 09:00:00', ?, 100.00, 100.00, 9, 1, 1, ?, 'PRINCIPAL', 1, 0, 1)
                """, fixture.bookIdA(), fixture.accountIdA(), legacyOrder);
        JsonNode removeLegacy = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/remove", fixture.bookIdA(), legacyOrder)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":0}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(removeLegacy.get("code").asInt()).isEqualTo(30018);
    }

    @Test
    @DisplayName("待确认流水阻止归档；归档保留历史；重开清时间")
    void pendingEntryBlocksCloseArchiveKeepsHistoryReopenClearsTime() throws Exception {
        Long orderId = createOrder("归档生命周期", 2, "2026-10-07");
        mockMvcAddEntry(orderId, 0L, 9, "1000.00", true);
        JsonNode unconfirmed = mockMvcAddEntry(orderId, 1L, 10, "1000.00", false);

        JsonNode blocked = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/close", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":2}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(blocked.get("code").asInt()).isEqualTo(30014);

        read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries/{billId}/confirm",
                        fixture.bookIdA(), orderId, unconfirmed.at("/data/billId").asLong())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":2}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));

        JsonNode closed = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/close", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":3}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(closed.get("code").asInt()).isEqualTo(SUCCESS);
        JsonNode archived = detail(orderId);
        assertThat(archived.get("data").get("status").asInt()).isEqualTo(1000);
        assertThat(archived.get("data").get("pendingAmount").asText()).isEqualTo("0.00");
        assertThat(archived.get("data").get("issuedAmount").asText()).isEqualTo("1000.00");
        assertThat(archived.get("data").get("closeTime").isNull()).isFalse();

        JsonNode archivedPage = read(mockMvc.perform(get("/fortune/finance/order/v2/page")
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("status", "1000"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(archivedPage.at("/data/rows").size()).isEqualTo(1);

        JsonNode reopened = read(mockMvc.perform(post(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/reopen", fixture.bookIdA(), orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":\"" + UUID.randomUUID() + "\",\"expectedVersion\":4}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(reopened.get("code").asInt()).isEqualTo(SUCCESS);
        JsonNode reopenedDetail = detail(orderId);
        assertThat(reopenedDetail.get("data").get("status").asInt()).isEqualTo(200);
        assertThat(reopenedDetail.get("data").get("closeTime").isNull()).isTrue();
    }

    @Test
    @DisplayName("分页22条取两页无重复；搜索不泄漏另一账本")
    void paginationTwentyTwoRecordsAndKeywordBookScope() throws Exception {
        for (int i = 1; i <= 22; i++) {
            createOrder("单据" + i, 2, null);
        }
        FinanceOrderCreateCommand otherBook = createCommand("单据23", 2);
        otherBook.setBookId(fixture.bookIdB());
        requestService.create(otherBook);

        JsonNode firstPage = read(mockMvc.perform(get("/fortune/finance/order/v2/page")
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("pageSize", "20")
                        .param("pageNum", "1"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        JsonNode secondPage = read(mockMvc.perform(get("/fortune/finance/order/v2/page")
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("pageSize", "20")
                        .param("pageNum", "2"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<Long> firstIds = orderIds(firstPage);
        List<Long> secondIds = orderIds(secondPage);
        assertThat(firstIds).hasSize(20);
        assertThat(secondIds).hasSize(2);
        Set<Long> union = new HashSet<>(firstIds);
        union.addAll(secondIds);
        assertThat(union).hasSize(22);
        assertThat(firstPage.get("data").get("total").asLong()).isEqualTo(22L);

        JsonNode keyword = read(mockMvc.perform(get("/fortune/finance/order/v2/page")
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("keyword", "单据2"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        List<Long> keywordIds = orderIds(keyword);
        List<Long> keywordBookIds = keywordIds.stream()
                .map(id -> jdbcTemplate.queryForObject(
                        "select book_id from fortune_finance_order where order_id = ?", Long.class, id))
                .toList();
        assertThat(keywordBookIds).allMatch(bookId -> bookId.equals(fixture.bookIdA()));
        assertThat(keyword.get("data").get("total").asLong()).isEqualTo(4L);
    }

    @Test
    @DisplayName("V2 modify清空字段与服务端时钟逾期")
    void modifyClearsFieldsAndServerClockOverdue() throws Exception {
        Long orderId = createOrder("字段清空", 2, "2026-10-07");
        mockMvcAddEntry(orderId, 0L, 9, "1000.00", true);

        JsonNode beforeModify = detail(orderId);
        assertThat(beforeModify.get("data").get("overdue").asBoolean()).isTrue();

        JsonNode modify = read(mockMvc.perform(put("/fortune/finance/order/v2/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(modifyJson(orderId, 1L)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(modify.get("code").asInt()).isEqualTo(SUCCESS);
        JsonNode detail = detail(orderId);
        assertThat(detail.get("data").get("remark").asText()).isEmpty();
        assertThat(detail.get("data").get("dueDate").isNull()).isTrue();
        assertThat(detail.get("data").get("overdue").asBoolean()).isFalse();

        Long todayOrder = createOrder("今日到期", 2, "2026-10-08");
        JsonNode todayDetail = detail(todayOrder);
        assertThat(todayDetail.get("data").get("overdue").asBoolean()).isFalse();

        JsonNode overduePage = read(mockMvc.perform(get("/fortune/finance/order/v2/page")
                        .param("bookId", String.valueOf(fixture.bookIdA()))
                        .param("overdueOnly", "true"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(orderIds(overduePage)).isEmpty();
    }

    @Test
    @DisplayName("V2 create非法type被拒绝")
    void v2CreateRejectsInvalidType() throws Exception {
        JsonNode invalid = read(mockMvc.perform(post("/fortune/finance/order/v2/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":" + fixture.bookIdA()
                                + ",\"requestId\":\"" + UUID.randomUUID()
                                + "\",\"title\":\"非法类型\",\"type\":9,\"counterpartyName\":\"张三\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(invalid.get("code").asInt()).isEqualTo(103);
    }

    private JsonNode detail(Long orderId) throws Exception {
        return read(mockMvc.perform(get("/fortune/finance/order/v2/{bookId}/{orderId}",
                        fixture.bookIdA(), orderId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private JsonNode mockMvcAddEntry(Long orderId, Long expectedVersion, Integer billType, String amount,
                                     boolean confirm) throws Exception {
        return read(mockMvc.perform(multipart(
                        "/fortune/finance/order/v2/{bookId}/{orderId}/entries", fixture.bookIdA(), orderId)
                        .file(dataPart(entryJson(orderId, expectedVersion, UUID.randomUUID().toString(),
                                billType, amount, fixture.accountIdA(), confirm, false))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Long createOrder(String title, Integer type, String dueDate) {
        FinanceOrderCreateCommand command = createCommand(title, type);
        command.setDueDate(dueDate == null ? null : LocalDate.parse(dueDate));
        return requestService.create(command).getOrderId();
    }

    private FinanceOrderCreateCommand createCommand(String title, Integer type) {
        FinanceOrderCreateCommand command = new FinanceOrderCreateCommand();
        command.setBookId(fixture.bookIdA());
        command.setRequestId(UUID.randomUUID().toString());
        command.setTitle(title);
        command.setType(type);
        command.setCounterpartyName("张三");
        command.setRemark("");
        return command;
    }

    private FinanceOrderEntryCommand entryCommand(Long orderId, Long expectedVersion, Integer billType,
                                                  String amount, Long accountId, boolean confirm, UUID requestId) {
        FinanceOrderEntryCommand entry = new FinanceOrderEntryCommand();
        entry.setBookId(fixture.bookIdB());
        entry.setOrderId(orderId);
        entry.setRequestId(requestId.toString());
        entry.setExpectedVersion(expectedVersion);
        entry.setTitle("借给张三");
        entry.setTradeTime(java.time.LocalDateTime.of(2026, 10, 1, 9, 0, 0));
        entry.setBillType(billType);
        entry.setOrderComponent("PRINCIPAL");
        entry.setAmount(new java.math.BigDecimal(amount));
        entry.setCategoryAmountPair(List.of());
        entry.setAccountId(accountId);
        entry.setConfirm(confirm);
        entry.setInclude(false);
        return entry;
    }

    private String createJson(String requestId, String title, Integer type, String dueDate) {
        return """
                {"bookId":%d,"requestId":"%s","title":"%s","type":%d,"counterpartyName":"张三","dueDate":%s,"remark":""}
                """.formatted(fixture.bookIdA(), requestId, title, type,
                dueDate == null ? null : "\"" + dueDate + "\"");
    }

    private String modifyJson(Long orderId, Long expectedVersion) {
        return """
                {"bookId":%d,"requestId":"%s","orderId":%d,"expectedVersion":%d,"title":"字段清空后","type":2,
                "counterpartyName":"张三","dueDate":null,"remark":""}
                """.formatted(fixture.bookIdA(), UUID.randomUUID(), orderId, expectedVersion);
    }

    private String entryJson(Long orderId, Long expectedVersion, String requestId, Integer billType,
                             String amount, Long accountId, boolean confirm, boolean include) {
        return """
                {"bookId":%d,"orderId":%d,"requestId":"%s","expectedVersion":%d,"title":"借给张三",
                "tradeTime":"2026-10-01 09:00:00","billType":%d,"orderComponent":"PRINCIPAL","amount":"%s",
                "categoryAmountPair":[],"accountId":%d,"confirm":%s,"include":%s}
                """.formatted(fixture.bookIdA(), orderId, requestId, expectedVersion, billType, amount,
                accountId, confirm, include);
    }

    private String orderLinkedBillJson(Long orderId, Long expectedVersion, Integer billType, String amount,
                                       boolean confirm) {
        return entryJson(orderId, expectedVersion, UUID.randomUUID().toString(), billType, amount,
                fixture.accountIdA(), confirm, false);
    }

    private MockMultipartFile dataPart(String json) {
        return new MockMultipartFile("data", null, MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    private List<Long> orderIds(JsonNode page) {
        List<Long> ids = new ArrayList<>();
        page.get("data").get("rows").forEach(row -> ids.add(row.get("orderId").asLong()));
        return ids;
    }

    private JsonNode read(String content) throws Exception {
        return JacksonUtil.from(content, JsonNode.class);
    }

    private void login(Long userId) {
        SystemLoginUser loginUser = new SystemLoginUser(userId, false, "user" + userId, "", null);
        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(loginUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
