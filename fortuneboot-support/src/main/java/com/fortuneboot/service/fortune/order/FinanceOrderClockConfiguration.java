package com.fortuneboot.service.fortune.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

import java.time.Clock;

@Configuration
public class FinanceOrderClockConfiguration {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock financeOrderClock() {
        return Clock.systemDefaultZone();
    }
}
