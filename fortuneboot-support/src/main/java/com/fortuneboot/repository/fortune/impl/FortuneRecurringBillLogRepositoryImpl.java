package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneRecurringBillLogMapper;
import com.fortuneboot.domain.entity.fortune.FortuneRecurringBillLogEntity;
import com.fortuneboot.repository.fortune.FortuneRecurringBillLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author zhangchi118
 * @date 2025/6/30 20:47
 **/
@Service
@AllArgsConstructor
public class FortuneRecurringBillLogRepositoryImpl extends ServiceImpl<FortuneRecurringBillLogMapper, FortuneRecurringBillLogEntity> implements FortuneRecurringBillLogRepository {

}
