package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderRequestMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderRequestEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderRequestRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 单据幂等请求表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@AllArgsConstructor
public class FortuneFinanceOrderRequestRepoImpl
        extends ServiceImpl<FortuneFinanceOrderRequestMapper, FortuneFinanceOrderRequestEntity>
        implements FortuneFinanceOrderRequestRepo {
}
