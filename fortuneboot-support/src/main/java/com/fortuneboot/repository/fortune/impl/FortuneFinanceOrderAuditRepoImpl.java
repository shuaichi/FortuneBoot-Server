package com.fortuneboot.repository.fortune.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fortuneboot.dao.fortune.FortuneFinanceOrderAuditMapper;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;
import com.fortuneboot.domain.query.fortune.order.FinanceOrderPageUtil;
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

    @Override
    public Page<FortuneFinanceOrderAuditEntity> pageByOrder(Long bookId, Long orderId,
                                                            Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<FortuneFinanceOrderAuditEntity> wrapper =
                new LambdaQueryWrapper<>(FortuneFinanceOrderAuditEntity.class);
        wrapper.eq(FortuneFinanceOrderAuditEntity::getBookId, bookId)
                .eq(FortuneFinanceOrderAuditEntity::getOrderId, orderId)
                .orderByDesc(FortuneFinanceOrderAuditEntity::getCreateTime)
                .orderByDesc(FortuneFinanceOrderAuditEntity::getId);
        return this.page(new Page<>(FinanceOrderPageUtil.pageNum(pageNum),
                FinanceOrderPageUtil.pageSize(pageSize)), wrapper);
    }
}
