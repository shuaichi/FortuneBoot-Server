package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderAuditMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.repository.fortune.FortuneFinanceOrderAuditRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 单据操作审计表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
@Service
@AllArgsConstructor
public class FortuneFinanceOrderAuditRepoImpl
        extends ServiceImpl<FortuneFinanceOrderAuditMapper, FortuneFinanceOrderAuditEntity>
        implements FortuneFinanceOrderAuditRepo {
}
