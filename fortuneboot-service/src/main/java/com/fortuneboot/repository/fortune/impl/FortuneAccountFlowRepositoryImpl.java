package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneAccountFlowMapper;
import com.fortuneboot.domain.entity.fortune.FortuneAccountFlowEntity;
import com.fortuneboot.repository.fortune.FortuneAccountFlowRepository;
import org.springframework.stereotype.Service;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:30
 **/
@Service
public class FortuneAccountFlowRepositoryImpl extends ServiceImpl<FortuneAccountFlowMapper, FortuneAccountFlowEntity> implements FortuneAccountFlowRepository {
}
