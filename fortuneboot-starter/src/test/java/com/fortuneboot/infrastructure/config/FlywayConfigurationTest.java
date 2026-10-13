package com.fortuneboot.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.annotation.DbType;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Flyway 迁移配置测试
 * <p>
 * GraalVM native image 下无法通过目录枚举发现迁移脚本，
 * {@link FlywayConfiguration#MIGRATION_FILES} 是唯一的降级数据源。
 * 这里保证该清单与迁移目录始终一致，避免新增脚本后 native 包静默漏迁移。
 */
class FlywayConfigurationTest {

    @Test
    @DisplayName("Native Image 降级清单应包含迁移目录中的所有脚本")
    void nativeFallbackList_containsAllMigrationScripts() throws IOException {
        for (String dbType : List.of(DbType.MYSQL.getDb(), DbType.SQLITE.getDb())) {
            List<String> declaredFiles = Arrays.asList(FlywayConfiguration.MIGRATION_FILES.get(dbType));
            for (String filename : listMigrationFilenames(dbType)) {
                assertThat(declaredFiles)
                    .as("%s 迁移目录中的 %s 未登记到 Native Image 降级清单", dbType, filename)
                    .contains(filename);
            }
        }
    }

    @Test
    @DisplayName("Native Image 降级清单中的脚本必须真实存在")
    void nativeFallbackList_containsExistingScriptsOnly() throws IOException {
        for (String dbType : List.of(DbType.MYSQL.getDb(), DbType.SQLITE.getDb())) {
            List<String> actualFiles = listMigrationFilenames(dbType);
            for (String declared : FlywayConfiguration.MIGRATION_FILES.get(dbType)) {
                assertThat(actualFiles)
                    .as("%s 清单中的 %s 在迁移目录中不存在", dbType, declared)
                    .contains(declared);
            }
        }
    }

    private List<String> listMigrationFilenames(String dbType) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/" + dbType + "/*.sql");
        return Arrays.stream(resources).map(Resource::getFilename).toList();
    }
}
