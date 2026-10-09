package com.fortuneboot.service.fortune.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuneboot.common.enums.fortune.FinanceOrderComponentEnum;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderCreateCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryCommand;
import com.fortuneboot.domain.command.fortune.order.FinanceOrderEntryMutation;
import com.fortuneboot.domain.dto.fortune.CategoryAmountDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据请求幂等哈希")
class FinanceOrderRequestHasherTest {

    private final FinanceOrderRequestHasher hasher = new FinanceOrderRequestHasher(new ObjectMapper());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("同一操作者与命令生成稳定哈希，字段变化改变哈希")
    void createHash_isStableUntilCommandChanges() {
        login("user1");
        FinanceOrderCreateCommand command = createCommand("报销单");
        String first = hasher.hashCreate(command);
        String second = hasher.hashCreate(command);

        command.setTitle("另一个标题");
        String changed = hasher.hashCreate(command);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64).isNotEqualTo(changed);
    }

    @Test
    @DisplayName("操作者参与哈希，不同操作者得到不同幂等指纹")
    void createHash_includesOperator() {
        login("user1");
        String user1 = hasher.hashCreate(createCommand("报销单"));
        login("user2");
        String user2 = hasher.hashCreate(createCommand("报销单"));

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("流水命令哈希覆盖组件、分类与附件内容")
    void entryHash_coversEntryCommandAndFiles() {
        login("user1");
        FinanceOrderEntryMutation mutation = entryMutation(new BigDecimal("100.00"));
        String first = hasher.hashEntry(mutation);
        String second = hasher.hashEntry(mutation);

        mutation.getEntryCommand().setAmount(new BigDecimal("101.00"));
        String changedAmount = hasher.hashEntry(mutation);

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(changedAmount);
    }

    @Test
    @DisplayName("附件字节内容影响流水幂等哈希")
    void entryHash_includesFileContent() {
        login("user1");
        FinanceOrderEntryMutation withFileA = entryMutation(new BigDecimal("100.00"));
        withFileA.setFileList(List.of(new MockMultipartFile("file", "a.txt", "text/plain", "a".getBytes())));
        FinanceOrderEntryMutation withFileB = entryMutation(new BigDecimal("100.00"));
        withFileB.setFileList(List.of(new MockMultipartFile("file", "a.txt", "text/plain", "b".getBytes())));

        assertThat(withFileA.getEntryCommand().getAmount())
                .isEqualByComparingTo(withFileB.getEntryCommand().getAmount());
        assertThat(hasher.hashEntry(withFileA)).isNotEqualTo(hasher.hashEntry(withFileB));
    }

    @Test
    @DisplayName("分类金额列表参与流水哈希")
    void entryHash_includesCategories() {
        login("user1");
        FinanceOrderEntryMutation first = entryMutation(new BigDecimal("100.00"));
        FinanceOrderEntryMutation second = entryMutation(new BigDecimal("100.00"));
        second.getEntryCommand().setCategoryAmountPair(List.of(
                new CategoryAmountDTO(1L, new BigDecimal("40.00")),
                new CategoryAmountDTO(2L, new BigDecimal("60.00"))));

        assertThat(hasher.hashEntry(first)).isNotEqualTo(hasher.hashEntry(second));
    }

    private void login(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(username, null, List.of()));
    }

    private FinanceOrderCreateCommand createCommand(String title) {
        FinanceOrderCreateCommand command = new FinanceOrderCreateCommand();
        command.setBookId(9L);
        command.setRequestId(UUID.randomUUID().toString());
        command.setTitle(title);
        command.setType(1);
        command.setCounterpartyName("张三");
        return command;
    }

    private FinanceOrderEntryMutation entryMutation(BigDecimal amount) {
        FinanceOrderEntryCommand entry = new FinanceOrderEntryCommand();
        entry.setBookId(9L);
        entry.setOrderId(30L);
        entry.setRequestId(UUID.randomUUID().toString());
        entry.setTitle("垫付");
        entry.setTradeTime(LocalDateTime.of(2026, 10, 1, 9, 0));
        entry.setAccountId(10L);
        entry.setBillType(7);
        entry.setOrderComponent(FinanceOrderComponentEnum.PRINCIPAL.getValue());
        entry.setAmount(amount);
        entry.setConfirm(true);
        entry.setInclude(true);
        entry.setCategoryAmountPair(List.of(new CategoryAmountDTO(1L, amount)));
        return FinanceOrderEntryMutation.builder()
                .bookId(9L)
                .orderId(30L)
                .operation(FinanceOrderEntryMutation.Operation.ADD)
                .entryCommand(entry)
                .requestId(entry.getRequestId())
                .expectedVersion(0L)
                .fileList(List.of())
                .build();
    }
}
