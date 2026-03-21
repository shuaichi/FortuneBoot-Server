package com.fortuneboot.infrastructure.config.natives;

import com.baomidou.mybatisplus.core.metadata.MapperProxyMetadata;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.extension.repository.AbstractRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
/**
 *
 * @author zhangchi118
 * @date 2026/1/8 23:35
 **/
@AutomaticFeature
public class MybatisPlusNativeFeature implements Feature {

    private static final Logger log = LoggerFactory.getLogger(MybatisPlusNativeFeature.class);

    /**
     * 👉 只扫描你自己的业务包（非常重要）
     */
    private static final String BASE_PACKAGE = "com.fortuneboot";

    @Override
    public void duringSetup(DuringSetupAccess access) {
        Set<Class<?>> lambdaClasses = scanLambdaCapturingClasses();

        for (Class<?> clazz : lambdaClasses) {
            RuntimeSerialization.registerLambdaCapturingClass(clazz);
            log.info("[Native][LambdaRegister] {}", clazz.getName());
        }

        log.info("[Native][LambdaRegister] total = {}", lambdaClasses.size());
    }

    private Set<Class<?>> scanLambdaCapturingClasses() {
        Set<Class<?>> result = new HashSet<>();

        try {
            var scanner = new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
                String className = metadataReader.getClassMetadata().getClassName();

                // 只扫描自己项目
                if (!className.startsWith(BASE_PACKAGE)) {
                    return false;
                }

                // 增加 ServiceImpl，Rest，Controller等后缀。
                // 只要类里面可能使用 SysMenuEntity::getMenuId 这种 Lambda，就需要被注册。
                return className.endsWith("RepoImpl")
                        || className.endsWith("Service")
                        || className.endsWith("ServiceImpl")
                        || className.endsWith("Factory")
                        || className.endsWith("Util")
                        || className.endsWith("Rest")
                        || className.endsWith("Controller");
            });

            var candidates = scanner.findCandidateComponents(BASE_PACKAGE);

            for (var bean : candidates) {
                Class<?> clazz = ClassUtils.forName(Objects.requireNonNull(bean.getBeanClassName()), null);
                result.add(clazz);
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to scan lambda capturing classes", e);
        }

        return result;
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {

        register(MybatisMapperProxy.class);
        register(MapperProxyMetadata.class);

        register(AbstractRepository.class);
        register(ServiceImpl.class);
    }

    private void register(Class<?> type) {
        RuntimeReflection.register(type);
        RuntimeReflection.register(type.getDeclaredConstructors());
        RuntimeReflection.register(type.getDeclaredMethods());
        RuntimeReflection.register(type.getDeclaredFields());
    }
}