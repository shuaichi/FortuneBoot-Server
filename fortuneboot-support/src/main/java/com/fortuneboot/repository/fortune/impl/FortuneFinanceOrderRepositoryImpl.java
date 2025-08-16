package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 单据表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2025/8/16 18:12
 **/
@Service
@AllArgsConstructor
public class FortuneFinanceOrderRepositoryImpl
        extends ServiceImpl<FortuneFinanceOrderMapper, FortuneFinanceOrderEntity>
        implements FortuneFinanceOrderRepository {

}
