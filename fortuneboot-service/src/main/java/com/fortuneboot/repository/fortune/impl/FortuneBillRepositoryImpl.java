package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneBillMapper;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;
import com.fortuneboot.repository.fortune.FortuneBillRepository;
import org.springframework.stereotype.Service;

/**
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/3 23:30
 **/
@Service
public class FortuneBillRepositoryImpl extends ServiceImpl<FortuneBillMapper, FortuneBillEntity> implements FortuneBillRepository {
}
