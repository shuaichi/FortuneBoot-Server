package com.fortuneboot.service.fortune.order;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class FinanceOrderSqliteTestSupport {

    private FinanceOrderSqliteTestSupport() {
    }

    public static Path createDatabase(String prefix) {
        try {
            Path database = Files.createTempFile(prefix, ".db");
            String url = "jdbc:sqlite:" + database.toAbsolutePath();
            Flyway.configure()
                    .dataSource(url, "", "")
                    .locations("classpath:db/migration/sqlite")
                    .load()
                    .migrate();
            return database;
        } catch (IOException exception) {
            throw new IllegalStateException("无法创建SQLite测试库", exception);
        }
    }

    public static void deleteDatabase(Path database) {
        try {
            Files.deleteIfExists(database);
            Files.deleteIfExists(Path.of(database + "-wal"));
            Files.deleteIfExists(Path.of(database + "-shm"));
        } catch (IOException ignored) {
            // 临时文件删除失败不影响测试结论。
        }
    }

    public static void cleanBusinessTables(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from fortune_finance_order_request");
        jdbcTemplate.update("delete from fortune_finance_order_audit");
        jdbcTemplate.update("delete from fortune_bill_extra");
        jdbcTemplate.update("delete from fortune_file");
        jdbcTemplate.update("delete from fortune_member_relation");
        jdbcTemplate.update("delete from fortune_tag_relation");
        jdbcTemplate.update("delete from fortune_category_relation");
        jdbcTemplate.update("delete from fortune_bill");
        jdbcTemplate.update("delete from fortune_finance_order");
        jdbcTemplate.update("delete from fortune_balance_snapshot");
        jdbcTemplate.update("delete from fortune_category");
        jdbcTemplate.update("delete from fortune_book");
        jdbcTemplate.update("delete from fortune_account");
        jdbcTemplate.update("delete from fortune_group");
    }

    public static Fixture insertFixture(JdbcTemplate jdbcTemplate, int orderType) {
        jdbcTemplate.update("""
                insert into fortune_group(group_id, group_name, default_currency, enable, default_book_id, remark, deleted)
                values (900, '阶段02分组', 'CNY', 1, 1, '', 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_book(book_id, group_id, book_name, default_currency, enable, deleted)
                values (901, 900, '阶段02账本', 'CNY', 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_category(category_id, category_type, category_name, book_id, parent_id, enable, deleted)
                values (1, 1, '阶段02分类', 901, 0, 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (1001, 900, '账户A', 1, 10000.00, 'CNY', 1, 1, 1, 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (1002, 900, '账户B', 1, 5000.00, 'CNY', 1, 1, 1, 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_finance_order(order_id, book_id, title, type, status, currency_code, version,
                    reconciliation_status, adjusted_amount, deleted)
                values (1, 901, '阶段02单据', ?, 100, 'CNY', 0, 'READY', 0, 0)
                """, orderType);
        return new Fixture(900L, 901L, 1001L, 1002L, 1L);
    }

    public static BigDecimal accountBalance(JdbcTemplate jdbcTemplate, Long accountId) {
        return jdbcTemplate.queryForObject("select balance from fortune_account where account_id = ?",
                BigDecimal.class, accountId);
    }

    public record Fixture(Long groupId, Long bookId, Long accountIdA, Long accountIdB, Long orderId) {
    }
}
