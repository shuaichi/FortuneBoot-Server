package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneBalanceFlowMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBalanceFlowEntity;
import com.fortuneboot.repository.fortune.FortuneBalanceFlowRepository;
import org.springframework.stereotype.Service;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:30
 **/
@Service
public class FortuneBalanceFlowRepositoryImpl extends ServiceImpl<FortuneBalanceFlowMapper, FortuneBalanceFlowEntity> implements FortuneBalanceFlowRepository {
}
