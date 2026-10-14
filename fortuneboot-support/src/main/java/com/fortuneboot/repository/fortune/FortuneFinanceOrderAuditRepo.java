package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderAuditEntity;

/**
 * 单据操作审计表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public interface FortuneFinanceOrderAuditRepo extends IService<FortuneFinanceOrderAuditEntity> {

    /**
     * 按单据分页查询审计记录，创建时间和ID倒序。
     *
     * @param bookId 账本id
     * @param orderId 单据id
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 审计分页结果
     */
    Page<FortuneFinanceOrderAuditEntity> pageByOrder(Long bookId, Long orderId, Integer pageNum, Integer pageSize);
}
