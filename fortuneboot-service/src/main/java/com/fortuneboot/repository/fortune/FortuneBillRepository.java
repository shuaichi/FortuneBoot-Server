package com.fortuneboot.repository.fortune;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fortuneboot.domain.entity.fortune.FortuneBillEntity;

/**
 * 账单流水Repository
 *
 * @Author work.chi.zhang@gmail.com
 * @Date 2024/06/03 23:29
 */
public interface FortuneBillRepository extends IService<FortuneBillEntity> {
    /**
     * 分页查询
     *
     * @param page
     * @param wrapper
     * @return
     */
    IPage<FortuneBillEntity> getPage(Page<FortuneBillEntity> page, LambdaQueryWrapper<FortuneBillEntity> wrapper);


}
