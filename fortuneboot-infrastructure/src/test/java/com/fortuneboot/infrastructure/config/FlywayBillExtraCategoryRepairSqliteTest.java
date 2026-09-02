package com.fortuneboot.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlywayBillExtraCategoryRepairSqliteTest {

    private Path databasePath;
    private String databaseUrl;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("fortuneboot-bill-extra-repair-", ".db");
        databaseUrl = "jdbc:sqlite:" + databasePath;
        migrateTo("1.7.0");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    @DisplayName("迁移仅修复非转账账单的空分类附加项，并记录可回滚审计")
    void migrate_repairsEligibleExtrasAndPreservesSkippedExtras() throws Exception {
        insertBill(1L, 1);
        insertExtra(101L, 1L, null);
        insertCategoryRelation(11L, 1L, 1001L, 0);

        insertBill(2L, 1);
        insertExtra(102L, 2L, null);
        insertCategoryRelation(22L, 2L, 2002L, 0);
        insertCategoryRelation(21L, 2L, 2001L, 0);

        insertBill(3L, 1);
        insertExtra(103L, 3L, null);

        insertBill(4L, 3);
        insertExtra(104L, 4L, null);
        insertCategoryRelation(41L, 4L, 4001L, 0);

        insertBill(5L, 1);
        insertExtra(105L, 5L, 5999L);
        insertCategoryRelation(51L, 5L, 5001L, 0);

        insertBill(6L, 1);
        insertExtra(106L, 6L, null);
        insertCategoryRelation(61L, 6L, 6001L, 1);
        insertCategoryRelation(62L, 6L, 6002L, 0);

        migrateLatest();

        assertThat(categoryIdOf(101L)).isEqualTo(1001L);
        assertThat(categoryIdOf(102L)).isEqualTo(2001L);
        assertThat(categoryIdOf(103L)).isNull();
        assertThat(categoryIdOf(104L)).isNull();
        assertThat(categoryIdOf(105L)).isEqualTo(5999L);
        assertThat(categoryIdOf(106L)).isEqualTo(6002L);

        assertThat(auditDecisionOf(101L)).isEqualTo("SINGLE_CATEGORY");
        assertThat(auditDecisionOf(102L)).isEqualTo("AUTO_FIRST_CATEGORY");
        assertThat(auditDecisionOf(103L)).isEqualTo("SKIP_NO_CATEGORY");
        assertThat(auditDecisionOf(106L)).isEqualTo("SINGLE_CATEGORY");
        assertThat(auditDecisionOf(104L)).isNull();
        assertThat(auditDecisionOf(105L)).isNull();

        assertThat(extraTypeOf(102L)).isEqualTo(1);
        assertThat(amountOf(102L)).isEqualByComparingTo("10.00");
        assertThat(accountSideOf(102L)).isEqualTo(1);
        assertThat(remarkOf(102L)).isEqualTo("unchanged");

        execute("UPDATE fortune_bill_extra SET category_id = 2999 WHERE extra_id = ?", 102L);
        executeRollback();

        assertThat(categoryIdOf(101L)).isNull();
        assertThat(categoryIdOf(102L)).isEqualTo(2999L);
        assertThat(categoryIdOf(103L)).isNull();
        assertThat(categoryIdOf(104L)).isNull();
        assertThat(categoryIdOf(105L)).isEqualTo(5999L);
        assertThat(categoryIdOf(106L)).isNull();
    }

    private void migrateTo(String target) {
        Flyway.configure()
                .dataSource(databaseUrl, null, null)
                .locations("classpath:db/migration/sqlite")
                .target(target)
                .load()
                .migrate();
    }

    private void migrateLatest() {
        Flyway.configure()
                .dataSource(databaseUrl, null, null)
                .locations("classpath:db/migration/sqlite")
                .load()
                .migrate();
    }

    private void insertBill(long billId, int billType) throws SQLException {
        execute("""
                INSERT INTO fortune_bill
                    (bill_id, book_id, title, bill_type, deleted)
                VALUES (?, 1, 'repair-test', ?, 0)
                """, billId, billType);
    }

    private void insertExtra(long extraId, long billId, Long categoryId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO fortune_bill_extra
                         (extra_id, bill_id, extra_type, amount, account_side, category_id, remark, deleted)
                     VALUES (?, ?, 1, 10.00, 1, ?, 'unchanged', 0)
                     """)) {
            statement.setLong(1, extraId);
            statement.setLong(2, billId);
            if (categoryId == null) {
                statement.setNull(3, java.sql.Types.BIGINT);
            } else {
                statement.setLong(3, categoryId);
            }
            statement.executeUpdate();
        }
    }

    private void insertCategoryRelation(long relationId, long billId, long categoryId, int deleted)
            throws SQLException {
        execute("""
                INSERT INTO fortune_category_relation
                    (category_relation_id, category_id, bill_id, amount, deleted)
                VALUES (?, ?, ?, 10.00, ?)
                """, relationId, categoryId, billId, deleted);
    }

    private void executeRollback() throws SQLException {
        execute("""
                UPDATE fortune_bill_extra
                SET category_id = (
                    SELECT audit.previous_category_id
                    FROM fortune_bill_extra_category_repair_audit audit
                    WHERE audit.migration_version = '1.8.0'
                      AND audit.extra_id = fortune_bill_extra.extra_id
                      AND audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
                )
                WHERE EXISTS (
                    SELECT 1
                    FROM fortune_bill_extra_category_repair_audit audit
                    WHERE audit.migration_version = '1.8.0'
                      AND audit.extra_id = fortune_bill_extra.extra_id
                      AND audit.decision IN ('SINGLE_CATEGORY', 'AUTO_FIRST_CATEGORY')
                      AND fortune_bill_extra.category_id IS audit.candidate_category_id
                )
                """);
    }

    private Long categoryIdOf(long extraId) throws SQLException {
        return queryNullableLong("SELECT category_id FROM fortune_bill_extra WHERE extra_id = ?", extraId);
    }

    private String auditDecisionOf(long extraId) throws SQLException {
        return queryString("""
                SELECT decision
                FROM fortune_bill_extra_category_repair_audit
                WHERE migration_version = '1.8.0' AND extra_id = ?
                """, extraId);
    }

    private int extraTypeOf(long extraId) throws SQLException {
        return queryInt("SELECT extra_type FROM fortune_bill_extra WHERE extra_id = ?", extraId);
    }

    private BigDecimal amountOf(long extraId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT amount FROM fortune_bill_extra WHERE extra_id = ?")) {
            statement.setLong(1, extraId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal(1);
            }
        }
    }

    private int accountSideOf(long extraId) throws SQLException {
        return queryInt("SELECT account_side FROM fortune_bill_extra WHERE extra_id = ?", extraId);
    }

    private String remarkOf(long extraId) throws SQLException {
        return queryString("SELECT remark FROM fortune_bill_extra WHERE extra_id = ?", extraId);
    }

    private void execute(String sql, Object... parameters) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            statement.executeUpdate();
        }
    }

    private Long queryNullableLong(String sql, long parameter) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                long value = resultSet.getLong(1);
                return resultSet.wasNull() ? null : value;
            }
        }
    }

    private int queryInt(String sql, long parameter) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private String queryString(String sql, long parameter) throws SQLException {
        try (Connection connection = DriverManager.getConnection(databaseUrl);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return resultSet.getString(1);
            }
        }
    }
}
