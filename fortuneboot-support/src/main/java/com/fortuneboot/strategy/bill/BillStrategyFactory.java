package com.fortuneboot.strategy.bill;

import com.fortuneboot.common.enums.fortune.BillTypeEnum;
import com.fortuneboot.common.exception.ApiException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author zhangchi118
 * @date 2025/8/25 21:39
 **/
@Component
public class BillStrategyFactory {

    /**
     * 策略缓存
     */
    private final Map<BillTypeEnum, BillProcessStrategy> strategyCache = new ConcurrentHashMap<>();

    @Resource
    private List<BillProcessStrategy> strategies;

    @PostConstruct
    public void initStrategies() {
        // 初始化策略缓存
        for (BillProcessStrategy strategy : strategies) {
            strategyCache.put(strategy.getSupportedBillType(), strategy);
        }
    }

    public BillProcessStrategy getStrategy(Integer billType) {
        BillTypeEnum billTypeEnum = BillTypeEnum.getByValue(billType);

        BillProcessStrategy strategy = strategyCache.get(billTypeEnum);
        if (Objects.isNull(strategy)) {
            // TODO 不支持的账单类型异常
//            throw new ApiException("不支持的账单类型: " + billType);
        }
        return strategy;
    }
}
