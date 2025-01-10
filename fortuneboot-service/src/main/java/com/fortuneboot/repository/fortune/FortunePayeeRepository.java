package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortunePayeeEntity;
import com.fortuneboot.domain.entity.fortune.FortuneTagEntity;

/**
 * 交易对象
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/6/5 23:19
 **/
public interface FortunePayeeRepository extends IService<FortunePayeeEntity> {

    /**
     * 根据账本id和名称查询
     *
     * @return
     */
    FortunePayeeEntity getByBookIdAndName(Long bookId, String payeeName);
}
