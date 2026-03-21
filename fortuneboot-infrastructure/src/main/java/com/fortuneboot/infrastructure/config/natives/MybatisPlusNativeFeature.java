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
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
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
    private static final String BASE_PACKAGE = "com.fortuneboot";

    @Override
    public void duringSetup(DuringSetupAccess access) {
        Set<Class<?>> lambdaClasses = scanLambdaCapturingClasses();
        for (Class<?> clazz : lambdaClasses) {
            RuntimeSerialization.registerLambdaCapturingClass(clazz);
        }
        log.info("[Native][LambdaRegister] 成功注册了 {} 个可能会使用 Lambda 表达式的类", lambdaClasses.size());
    }

    private Set<Class<?>> scanLambdaCapturingClasses() {
        Set<Class<?>> result = new HashSet<>();
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> {
                String className = metadataReader.getClassMetadata().getClassName();
                // 【核心修复】不再根据后缀判断，全局扫描我们自己的包，确保万无一失
                return className.startsWith(BASE_PACKAGE);
            });
            var candidates = scanner.findCandidateComponents(BASE_PACKAGE);
            for (var bean : candidates) {
                Class<?> clazz = ClassUtils.forName(Objects.requireNonNull(bean.getBeanClassName()), null);
                result.add(clazz);
            }
        } catch (Exception e) {
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