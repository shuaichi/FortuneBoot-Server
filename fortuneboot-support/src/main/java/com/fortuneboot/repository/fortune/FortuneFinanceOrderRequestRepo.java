package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneFinanceOrderRequestEntity;

/**
 * 单据幂等请求表
 *
 * @author work.chi.zhang@gmail.com
 * @date 2026/10/9
 */
public interface FortuneFinanceOrderRequestRepo extends IService<FortuneFinanceOrderRequestEntity> {

    /**
     * 按账本和请求ID查询幂等请求回执。
     *
     * @param bookId 账本id
     * @param requestId 请求id
     * @return 请求回执，不存在时返回null
     */
    FortuneFinanceOrderRequestEntity findByBookIdAndRequestId(Long bookId, String requestId);
}
