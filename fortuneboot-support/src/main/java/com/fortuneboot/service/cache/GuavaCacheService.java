package com.fortuneboot.service.cache;


import com.fortuneboot.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import com.fortuneboot.repository.system.SysConfigRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author valarchie
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GuavaCacheService {

    private final SysConfigRepo configRepository;


    public final AbstractGuavaCacheTemplate<String> configCache = new AbstractGuavaCacheTemplate<String>() {
        @Override
        public String getObjectFromDb(Object id) {
            return configRepository.getConfigValueByKey(id.toString());
        }
    };
}
