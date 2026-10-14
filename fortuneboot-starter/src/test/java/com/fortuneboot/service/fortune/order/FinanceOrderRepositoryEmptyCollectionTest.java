package com.fortuneboot.service.fortune.order;

import com.fortuneboot.repository.fortune.FortuneAccountRepo;
import com.fortuneboot.repository.fortune.FortuneBillRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRelationRepo;
import com.fortuneboot.repository.fortune.FortuneCategoryRepo;
import com.fortuneboot.repository.fortune.FortuneGroupRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRelationRepo;
import com.fortuneboot.repository.fortune.FortuneMemberRepo;
import com.fortuneboot.repository.fortune.FortunePayeeRepo;
import com.fortuneboot.repository.fortune.FortuneTagRelationRepo;
import com.fortuneboot.repository.fortune.FortuneTagRepo;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("单据仓库空集合批量查询不生成IN()")
@SpringBootTest(classes = FinanceOrderSqliteTestApplication.class)
class FinanceOrderRepositoryEmptyCollectionTest {

    private static final Path DATABASE = FinanceOrderSqliteTestSupport.createDatabase(
            "finance-order-empty-collection-");
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE.toAbsolutePath();
    private static final List<String> PREPARED_SQL = new CopyOnWriteArrayList<>();
    private static final EmptyInSqlRecorder SQL_RECORDER = new EmptyInSqlRecorder();
    private static boolean recorderRegistered;

    @Autowired
    private FortuneTagRepo tagRepo;

    @Autowired
    private FortuneCategoryRepo categoryRepo;

    @Autowired
    private FortuneMemberRepo memberRepo;

    @Autowired
    private FortuneAccountRepo accountRepo;

    @Autowired
    private FortunePayeeRepo payeeRepo;

    @Autowired
    private FortuneGroupRepo groupRepo;

    @Autowired
    private FortuneBillRepo billRepo;

    @Autowired
    private FortuneCategoryRelationRepo categoryRelationRepo;

    @Autowired
    private FortuneTagRelationRepo tagRelationRepo;

    @Autowired
    private FortuneMemberRelationRepo memberRelationRepo;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("finance-order.test.sqlite-url", () -> DATABASE_URL);
    }

    @AfterAll
    static void cleanDatabase() {
        FinanceOrderSqliteTestSupport.deleteDatabase(DATABASE);
    }

    @BeforeEach
    void registerSqlRecorder() {
        PREPARED_SQL.clear();
        if (!recorderRegistered) {
            sqlSessionFactory.getConfiguration().addInterceptor(SQL_RECORDER);
            recorderRegistered = true;
        }
    }

    @Test
    @DisplayName("空集合返回空结果且不产生IN()")
    void emptyCollectionQueries_doNotGenerateEmptyInClause() {
        assertThat(tagRepo.getByIds(List.of())).isEmpty();
        assertThat(tagRepo.getByParentIds(List.of())).isEmpty();
        assertThat(categoryRepo.getByParentIds(List.of())).isEmpty();
        assertThat(memberRepo.getByIds(List.of())).isEmpty();
        assertThat(accountRepo.getByIds(List.of())).isEmpty();
        assertThat(payeeRepo.getByIdList(List.of())).isEmpty();
        assertThat(groupRepo.getEnableByGroupIds(List.of())).isEmpty();
        assertThat(billRepo.getByBookIds(List.of())).isEmpty();
        categoryRepo.removeByBookIds(List.of());
        tagRepo.removeByBookIds(List.of());
        payeeRepo.removeByBookIds(List.of());
        memberRepo.removeByBookIds(List.of());
        assertThat(categoryRelationRepo.getByBillIdList(List.of())).isEmpty();
        assertThat(tagRelationRepo.getByBillIdList(List.of())).isEmpty();
        assertThat(memberRelationRepo.getByBillIdList(List.of())).isEmpty();

        assertThat(PREPARED_SQL)
                .as("空集合批量查询不应生成非法的IN()片段")
                .noneMatch(sql -> sql.toLowerCase(Locale.ROOT).contains(" in ()"));
    }

    @Intercepts(@Signature(type = StatementHandler.class, method = "prepare",
            args = {Connection.class, Integer.class}))
    private static final class EmptyInSqlRecorder implements Interceptor {

        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            PREPARED_SQL.add(handler.getBoundSql().getSql());
            return invocation.proceed();
        }

        @Override
        public Object plugin(Object target) {
            return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
        }
    }
}
