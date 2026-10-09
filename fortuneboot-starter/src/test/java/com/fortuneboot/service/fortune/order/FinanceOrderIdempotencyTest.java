package com.fortuneboot.service.fortune.order;

import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.common.exception.ApiException;
import com.fortuneboot.common.exception.error.ErrorCode;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.vo.fortune.order.FinanceOrderMutationVo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("单据写入口幂等")
@SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
class FinanceOrderIdempotencyTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase("finance-order-stage02-idem-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @Autowired
    private FinanceOrderRequestService requestService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FinanceOrderSqliteTestSupport.Fixture fixture;

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void setUp() {
        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        fixture = FinanceOrderSqliteTestSupport.insertFixture(jdbcTemplate, 2);
    }

    @Test
    @DisplayName("同requestId同内容重放返回原billId和version，不重复写现金")
    void sameRequestIdAndHash_replaysOriginalResult() {
        String requestId = UUID.randomUUID().toString();
        FinanceOrderMutationVo first = requestService.mutateEntry(addMutation(requestId, "1000.00"));
        FinanceOrderMutationVo replay = requestService.mutateEntry(addMutation(requestId, "1000.00"));

        assertThat(replay.getReplayed()).isTrue();
        assertThat(replay.getBillId()).isEqualTo(first.getBillId());
        assertThat(replay.getVersion()).isEqualTo(first.getVersion());
        assertThat(billCount()).isEqualTo(1);
        assertThat(FinanceOrderSqliteTestSupport.accountBalance(jdbcTemplate, fixture.accountIdA()))
                .isEqualByComparingTo("9000.00");
    }

    @Test
    @DisplayName("同requestId换金额返回冲突且不写第二笔")
    void sameRequestIdWithDifferentHash_conflicts() {
        String requestId = UUID.randomUUID().toString();
        requestService.mutateEntry(addMutation(requestId, "1000.00"));

        assertThatThrownBy(() -> requestService.mutateEntry(addMutation(requestId, "900.00")))
                .isInstanceOf(ApiException.class)
                .extracting(exception -> ((ApiException) exception).getErrorCode())
                .isEqualTo(ErrorCode.Business.ORDER_REQUEST_CONFLICT);
        assertThat(billCount()).isEqualTo(1);
    }

    private FinanceOrderEntryMutation addMutation(String requestId, String amount) {
        FinanceOrderEntryCommand command = new FinanceOrderEntryCommand();
        command.setBookId(fixture.bookId());
        command.setOrderId(fixture.orderId());
        command.setTitle("幂等流水");
        command.setTradeTime(LocalDateTime.of(2026, 10, 2, 10, 0));
        command.setAccountId(fixture.accountIdA());
        command.setBillType(9);
        command.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        command.setConfirm(true);
        command.setInclude(true);
        command.setAmount(new BigDecimal(amount));
        command.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, new BigDecimal(amount))));
        command.setRequestId(requestId);
        command.setExpectedVersion(currentVersion());

        return FinanceOrderEntryMutation.builder()
                .bookId(fixture.bookId())
                .orderId(fixture.orderId())
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(command)
                .requestId(requestId)
                .expectedVersion(currentVersion())
                .build();
    }

    private long currentVersion() {
        Long version = jdbcTemplate.queryForObject(
                "select version from fortune_finance_order where order_id = ?", Long.class, fixture.orderId());
        assertThat(version).isNotNull();
        return version;
    }

    private int billCount() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from fortune_bill", Integer.class);
        assertThat(count).isNotNull();
        return count;
    }
}
