package com.fortuneboot.service.fortune.order;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 阶段03测试数据支撑：双分组/三账本/双账户/交易对象/用户分组关系。
 */
public final class FinanceOrderStage03TestSupport {

    private FinanceOrderStage03TestSupport() {
    }

    public static void cleanAllTables(JdbcTemplate jdbcTemplate) {
        FinanceOrderSqliteTestSupport.cleanBusinessTables(jdbcTemplate);
        jdbcTemplate.update("delete from fortune_payee");
        jdbcTemplate.update("delete from fortune_user_group_relation");
    }

    public static Stage03Fixture insertFixture(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("""
                insert into fortune_group(group_id, group_name, default_currency, enable, default_book_id, remark, deleted)
                values (900, '阶段03分组', 'CNY', 1, 901, '', 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_group(group_id, group_name, default_currency, enable, default_book_id, remark, deleted)
                values (910, '阶段03分组B', 'CNY', 1, 911, '', 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_book(book_id, group_id, book_name, default_currency, enable, deleted)
                values (901, 900, '账本A', 'CNY', 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_book(book_id, group_id, book_name, default_currency, enable, deleted)
                values (902, 900, '账本B', 'CNY', 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_book(book_id, group_id, book_name, default_currency, enable, deleted)
                values (911, 910, '账本C', 'CNY', 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_category(category_id, category_type, category_name, book_id, parent_id, enable, deleted)
                values (1, 1, '阶段03分类', 901, 0, 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (1001, 900, '账户A', 1, 10000.00, 'CNY', 1, 1, 1, 1, 0)
                """);
        jdbcTemplate.update("update fortune_account set can_transfer_out = 1, can_transfer_in = 1 where account_id = 1001");
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (1002, 900, '账户B', 1, 5000.00, 'CNY', 1, 1, 1, 1, 0)
                """);
        jdbcTemplate.update("update fortune_account set can_transfer_out = 1, can_transfer_in = 1 where account_id = 1002");
        jdbcTemplate.update("""
                insert into fortune_account(account_id, group_id, account_name, account_type, balance, currency_code,
                    enable, include, can_expense, can_income, deleted)
                values (1011, 910, '账户C', 1, 7000.00, 'CNY', 1, 1, 1, 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_payee(payee_id, book_id, payee_name, can_expense, can_income, enable, recycle_bin, deleted)
                values (1, 901, '张三', 1, 1, 1, 0, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_user_group_relation(role_type, group_id, user_id, default_group, deleted)
                values (1, 900, 1, 1, 0)
                """);
        jdbcTemplate.update("""
                insert into fortune_user_group_relation(role_type, group_id, user_id, default_group, deleted)
                values (3, 900, 2, 0, 0)
                """);
        return new Stage03Fixture(900L, 901L, 902L, 910L, 911L, 1001L, 1002L, 1011L, 1L, 1L, 2L, 3L);
    }

    public record Stage03Fixture(Long groupId, Long bookIdA, Long bookIdB, Long secondGroupId, Long bookIdC,
                                 Long accountIdA, Long accountIdB, Long accountIdC, Long payeeId,
                                 Long ownerUserId, Long visitorUserId, Long outsiderUserId) {
    }
}
