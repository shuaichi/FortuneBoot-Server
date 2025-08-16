package com.fortuneboot.service.fortune;

import com.fortuneboot.factory.fortune.factory.FortuneFinanceOrderFactory;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 单据
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:19
 **/
@Service
@AllArgsConstructor
public class FortuneFinanceOrderService {

    private final FortuneFinanceOrderFactory fortuneFinanceOrderFactory;

    private final FortuneFinanceOrderRepository fortuneFinanceOrderRepository;


}
