package com.fortuneboot.service.fortune.order;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 单据账本 V1.8.0 双库迁移测试
 * <p>
 * MySQL 用例必须通过 FINANCE_ORDER_TEST_MYSQL_URL / FINANCE_ORDER_TEST_MYSQL_USER /
 * FINANCE_ORDER_TEST_MYSQL_PASSWORD 显式指向专用测试 schema（库名包含 test），
 * 禁止回退应用默认库；该 schema 内所有表会在测试开始时被清空。
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@DisplayName("单据账本V1.8.0双库迁移")
class FinanceOrderMigrationTest {

    private static final String PREVIOUS_VERSION = "1.7.1";
    private static final String TARGET_VERSION = "1.8.0";
    private static final int SQLITE_BASELINE_MIGRATION_COUNT = 5;
    private static final int MYSQL_BASELINE_MIGRATION_COUNT = 11;

    @Test
    @DisplayName("SQLite: 历史现金与流水不变，新增默认值/索引/唯一键正确，重复迁移no-op")
    void sqliteMigration_preservesHistory_andAddsLedgerStructure() throws Exception {
        Path dbFile = Files.createTempFile("finance-order-stage01-sqlite-", ".db");
        try {
            String url = "jdbc:sqlite:" + dbFile.toAbsolutePath();
            runMigrationScenario(url, "", "", "sqlite", SQLITE_BASELINE_MIGRATION_COUNT);
        } finally {
            Files.deleteIfExists(dbFile);
            Files.deleteIfExists(resolveSibling(dbFile, "-wal"));
            Files.deleteIfExists(resolveSibling(dbFile, "-shm"));
        }
    }

    @Test
    @DisplayName("MySQL: 历史现金与流水不变，新增默认值/索引/唯一键正确，重复迁移no-op")
    void mysqlMigration_preservesHistory_andAddsLedgerStructure() throws Exception {
        String url = System.getenv("FINANCE_ORDER_TEST_MYSQL_URL");
        String user = System.getenv("FINANCE_ORDER_TEST_MYSQL_USER");
        String password = System.getenv("FINANCE_ORDER_TEST_MYSQL_PASSWORD");
        Assumptions.assumeTrue(url != null && user != null && password != null,
            "未配置 FINANCE_ORDER_TEST_MYSQL_URL/USER/PASSWORD，MySQL迁移验证阻塞");
        requireTestSchema(url);
        cleanMysqlSchema(url, user, password);
        runMigrationScenario(url, user, password, "mysql", MYSQL_BASELINE_MIGRATION_COUNT);
    }

    private void runMigrationScenario(String url, String user, String password,
                                      String dbType, int baselineMigrationCount) throws Exception {
        MigrateResult baselineResult = flyway(url, user, password, dbType, PREVIOUS_VERSION).migrate();
        assertThat(baselineResult.migrationsExecuted)
            .as("应完整执行既有Flyway链到V1.7.1")
            .isEqualTo(baselineMigrationCount);

        insertHistorySamples(url, user, password);

        MigrateResult ledgerResult = flyway(url, user, password, dbType, TARGET_VERSION).migrate();
        assertThat(ledgerResult.migrationsExecuted)
            .as("V1.8.0应只执行一个迁移")
            .isEqualTo(1);

        assertHistoryUnchanged(url, user, password, dbType);
        assertNewColumnsAndDefaults(url, user, password, dbType);
        assertIndexes(url, user, password, dbType);
        assertRequestIdempotentTable(url, user, password);
        assertAuditTable(url, user, password);

        MigrateResult noopResult = flyway(url, user, password, dbType, TARGET_VERSION).migrate();
        assertThat(noopResult.migrationsExecuted)
            .as("再次Flyway migrate应为no-op")
            .isZero();
    }

    private Flyway flyway(String url, String user, String password, String dbType, String target) {
        return Flyway.configure()
            .dataSource(url, user, password)
            .locations("classpath:db/migration/" + dbType)
            .target(target)
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .outOfOrder(false)
            .load();
    }

    private void insertHistorySamples(String url, String user, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("insert into fortune_group (group_id, group_name, default_currency, enable, default_book_id, remark, deleted)"
                + " values (901, '迁移测试分组', 'CNY', 1, 0, '', 0)");
            statement.executeUpdate("insert into fortune_book (book_id, group_id, book_name, default_currency, enable, deleted)"
                + " values (901, 901, '迁移测试账本', 'CNY', 1, 0)");
            statement.executeUpdate("insert into fortune_account (account_id, account_name, balance, currency_code, enable, account_type, group_id, deleted)"
                + " values (901, '账户A', 10000.0000, 'CNY', 1, 1, 1, 0)");
            statement.executeUpdate("insert into fortune_finance_order (order_id, book_id, title, type, out_amount, in_amount, status, remark, deleted)"
                + " values (901, 901, '历史费用报销单', 1, 800.0000, 300.0000, 200, '', 0)");
            statement.executeUpdate("insert into fortune_bill (bill_id, book_id, title, trade_time, account_id, amount, converted_amount,"
                + " order_id, bill_type, confirm, include, recycle_bin, remark, deleted)"
                + " values (901, 901, '历史垫付', '2026-09-01 10:00:00', 1, 800.0000, 800.0000, 1, 7, 1, 0, 0, '', 0)");
        }
    }

    private void assertHistoryUnchanged(String url, String user, String password, String dbType) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                "select balance from fortune_account where account_id = 901")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getBigDecimal("balance"))
                    .as("历史账户现金余额不能被迁移改变")
                    .isEqualByComparingTo(new BigDecimal("10000.0000"));
            }
            try (ResultSet resultSet = statement.executeQuery(
                "select amount, converted_amount, confirm, deleted, recycle_bin from fortune_bill where bill_id = 901")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getBigDecimal("amount")).isEqualByComparingTo(new BigDecimal("800.0000"));
                assertThat(resultSet.getBigDecimal("converted_amount")).isEqualByComparingTo(new BigDecimal("800.0000"));
                assertThat(resultSet.getInt("confirm")).isEqualTo(1);
                assertThat(resultSet.getInt("deleted")).isZero();
                assertThat(resultSet.getInt("recycle_bin")).isZero();
            }
            try (ResultSet resultSet = statement.executeQuery(
                "select out_amount, in_amount, status from fortune_finance_order where order_id = 901")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getBigDecimal("out_amount")).isEqualByComparingTo(new BigDecimal("800.0000"));
                assertThat(resultSet.getBigDecimal("in_amount")).isEqualByComparingTo(new BigDecimal("300.0000"));
                assertThat(resultSet.getInt("status")).isEqualTo(200);
            }
        }
    }

    private void assertNewColumnsAndDefaults(String url, String user, String password, String dbType) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                "select counterparty_id, counterparty_name, currency_code, due_date, adjusted_amount, version,"
                    + " reconciliation_status, reconciliation_note from fortune_finance_order where order_id = 901")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getObject("counterparty_id")).isNull();
                assertThat(resultSet.getString("counterparty_name")).isNull();
                assertThat(resultSet.getString("currency_code")).isNull();
                assertThat(resultSet.getObject("due_date")).isNull();
                assertThat(resultSet.getBigDecimal("adjusted_amount")).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(resultSet.getLong("version")).isZero();
                assertThat(resultSet.getString("reconciliation_status")).isEqualTo("REVIEW_REQUIRED");
                assertThat(resultSet.getString("reconciliation_note")).isNull();
            }
            try (ResultSet resultSet = statement.executeQuery(
                "select order_component, order_ledger_version from fortune_bill where bill_id = 901")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("order_component")).isNull();
                assertThat(resultSet.getInt("order_ledger_version")).isZero();
            }

            statement.executeUpdate("insert into fortune_finance_order (book_id, title, type, status, remark)"
                + " values (901, '新规则单据', 2, 100, '')");
            try (ResultSet resultSet = statement.executeQuery(
                "select counterparty_id, adjusted_amount, version, reconciliation_status from fortune_finance_order where order_id <> 901")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getObject("counterparty_id")).isNull();
                assertThat(resultSet.getBigDecimal("adjusted_amount")).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(resultSet.getLong("version")).isZero();
                assertThat(resultSet.getString("reconciliation_status")).isEqualTo("REVIEW_REQUIRED");
            }

            statement.executeUpdate("insert into fortune_bill (book_id, title, bill_type, confirm, include)"
                + " values (901, '新规则流水', 9, 1, 0)");
            try (ResultSet resultSet = statement.executeQuery(
                "select order_component, order_ledger_version from fortune_bill where title = '新规则流水'")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("order_component")).isNull();
                assertThat(resultSet.getInt("order_ledger_version")).isZero();
            }
        }
    }

    private void assertIndexes(String url, String user, String password, String dbType) throws SQLException {
        assertIndexExists(url, user, password, dbType, "fortune_finance_order", "idx_finance_order_book_type_status");
        assertIndexExists(url, user, password, dbType, "fortune_finance_order", "idx_finance_order_book_due_date");
        assertIndexExists(url, user, password, dbType, "fortune_bill", "idx_fortune_bill_book_order_ledger");
        assertIndexExists(url, user, password, dbType, "fortune_finance_order_request", "uk_finance_order_request");
    }

    private void assertIndexExists(String url, String user, String password, String dbType,
                                   String tableName, String indexName) throws SQLException {
        String sql = "sqlite".equals(dbType)
            ? "select count(*) from sqlite_master where type = 'index' and name = ?"
            : "select count(*) from information_schema.statistics where table_schema = database()"
                + " and table_name = ? and index_name = ?";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if ("sqlite".equals(dbType)) {
                statement.setString(1, indexName);
            } else {
                statement.setString(1, tableName);
                statement.setString(2, indexName);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getInt(1))
                    .as("索引 %s 应存在", indexName)
                    .isPositive();
            }
        }
    }

    private void assertRequestIdempotentTable(String url, String user, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("insert into fortune_finance_order_request"
                + " (book_id, request_id, operation, request_hash, creator_id, response_json, create_time)"
                + " values (901, 'req-stage01', 'CREATE', 'hash-stage01', 1, '{}', '2026-10-09 10:00:00')");

            assertThatThrownBy(() -> statement.executeUpdate("insert into fortune_finance_order_request"
                    + " (book_id, request_id, operation, request_hash, creator_id, response_json, create_time)"
                    + " values (901, 'req-stage01', 'CREATE', 'hash-stage01-conflict', 1, '{}', '2026-10-09 10:00:01')"))
                .as("(book_id, request_id)唯一键必须生效")
                .isInstanceOf(SQLException.class);

            try (ResultSet resultSet = statement.executeQuery(
                "select book_id, request_id, operation, request_hash, response_json from fortune_finance_order_request")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong("book_id")).isEqualTo(901L);
                assertThat(resultSet.getString("request_id")).isEqualTo("req-stage01");
                assertThat(resultSet.getString("operation")).isEqualTo("CREATE");
                assertThat(resultSet.getString("request_hash")).isEqualTo("hash-stage01");
                assertThat(resultSet.getString("response_json")).isEqualTo("{}");
            }
        }
    }

    private void assertAuditTable(String url, String user, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("insert into fortune_finance_order_audit"
                + " (book_id, order_id, bill_id, request_id, action, before_json, after_json, creator_id, create_time)"
                + " values (901, 901, null, 'req-stage01', 'CREATE', null, '{}', 1, '2026-10-09 10:00:00')");
            try (ResultSet resultSet = statement.executeQuery(
                "select book_id, order_id, bill_id, request_id, action, before_json, after_json"
                    + " from fortune_finance_order_audit")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong("book_id")).isEqualTo(901L);
                assertThat(resultSet.getLong("order_id")).isEqualTo(901L);
                assertThat(resultSet.getObject("bill_id")).isNull();
                assertThat(resultSet.getString("request_id")).isEqualTo("req-stage01");
                assertThat(resultSet.getString("action")).isEqualTo("CREATE");
                assertThat(resultSet.getString("before_json")).isNull();
                assertThat(resultSet.getString("after_json")).isEqualTo("{}");
            }
        }
    }

    private void requireTestSchema(String url) {
        String withoutQuery = url.split("\\?")[0];
        String database = withoutQuery.substring(withoutQuery.lastIndexOf('/') + 1);
        assertThat(database.toLowerCase(Locale.ROOT))
            .as("MySQL迁移测试只允许连接名称包含test的专用测试schema")
            .contains("test");
    }

    private void cleanMysqlSchema(String url, String user, String password) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(
                "select table_name from information_schema.tables where table_schema = database()")) {
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString(1));
                }
            }
            statement.execute("set foreign_key_checks = 0");
            for (String tableName : tableNames) {
                statement.executeUpdate("drop table if exists `" + tableName + "`");
            }
            statement.execute("set foreign_key_checks = 1");
        }
    }

    private Path resolveSibling(Path path, String suffix) {
        String name = path.getFileName().toString() + suffix;
        return path.resolveSibling(name);
    }
}
